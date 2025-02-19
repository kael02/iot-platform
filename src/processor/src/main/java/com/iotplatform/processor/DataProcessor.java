package com.iotplatform.processor;

import org.apache.pulsar.client.api.*;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.clickhouse.jdbc.ClickHouseDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class DataProcessor implements Function<byte[], Void> {
    private static final String DB_URL = "jdbc:clickhouse://clickhouse-single.clickhouse.svc.cluster.local:8123?compress=0";

    private static final String DB_USER = System.getenv("username");
    private static final String DB_PASSWORD = System.getenv("password");
    private static final String INPUT_TOPIC = "persistent://public/default/sensor-data";
    
    private static final String BROKER_URL = "pulsar://pulsar-proxy.pulsar.svc.cluster.local:6650";

    private ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        createTableIfNotExists();

        PulsarClient client = PulsarClient.builder()
                .serviceUrl(BROKER_URL)
                .build();

        Consumer<byte[]> consumer = client.newConsumer()
                .topic(INPUT_TOPIC)
                .subscriptionName("sensor-processor-sub")
                .subscriptionType(SubscriptionType.Exclusive)
                .subscribe();

        DataProcessor processor = new DataProcessor();

        System.out.println("Started processing messages..");

        while (true) {
            try {
                Message<byte[]> msg = consumer.receive(1, TimeUnit.SECONDS);
                if (msg != null) {
                    processor.process(msg.getValue(), null);
                    consumer.acknowledge(msg);
                    System.out.println("Processed and acknowledged message");
                }
            } catch (Exception e) {
                System.err.println("Error processing message: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void createTableIfNotExists() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("user", DB_USER);
        properties.setProperty("password", DB_PASSWORD);
        System.out.println("DB_URL: " + DB_URL);
        System.out.println("DB_USER: " + DB_USER);
        System.out.println("DB_PASSWORD: " + DB_PASSWORD);
        try (Connection conn = new ClickHouseDataSource(DB_URL, properties).getConnection()) {
            String createTableSQL = 
                "CREATE TABLE IF NOT EXISTS sensor_data (" +
                "    sensor_id String," +
                "    temperature Float64," +
                "    humidity Float64," +
                "    timestamp DateTime," +
                "    is_anomaly UInt8" +
                ") ENGINE = MergeTree()" +
                "ORDER BY (timestamp, sensor_id)";
            
            conn.createStatement().execute(createTableSQL);
        }
    }

    @Override
    public Void process(byte[] input, Context context) throws Exception {
        try {
            SensorData data = mapper.readValue(input, SensorData.class);
            boolean isAnomaly = isAnomalous(data);

            Properties properties = new Properties();
            properties.setProperty("user", DB_USER);
            properties.setProperty("password", DB_PASSWORD);

            try (Connection conn = new ClickHouseDataSource(DB_URL, properties).getConnection()) {
                String sql = "INSERT INTO sensor_data (sensor_id, temperature, humidity, timestamp, is_anomaly) " +
                        "VALUES (?, ?, ?, ?, ?)";

                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, data.getSensorId());
                stmt.setDouble(2, data.getTemperature());
                stmt.setDouble(3, data.getHumidity());
                stmt.setTimestamp(4, java.sql.Timestamp.valueOf(data.getTimestamp()));
                stmt.setInt(5, isAnomaly ? 1 : 0);  

                stmt.executeUpdate();
                System.out.println("Stored data in ClickHouse: " + mapper.writeValueAsString(data));
            }
        } catch (Exception e) {
            System.err.println("Error processing data: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return null;
    }

    private boolean isAnomalous(SensorData data) {
        return data.getTemperature() > 90 || data.getTemperature() < 0 ||
                data.getHumidity() > 100 || data.getHumidity() < 0;
    }
}