package com.iotplatform.processor;

import org.apache.pulsar.client.api.*;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.concurrent.TimeUnit;

public class DataProcessor implements Function<byte[], Void> {
    private static final String DB_URL = "jdbc:postgresql://postgres:5432/sensordb";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgres";
    private static final String INPUT_TOPIC = "persistent://public/default/sensor-data";
    private static final String BROKER_URL = "pulsar://pulsar:6650";

    private ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        PulsarClient client = PulsarClient.builder()
                .serviceUrl(BROKER_URL)
                .build();

        Consumer<byte[]> consumer = client.newConsumer()
                .topic(INPUT_TOPIC)
                .subscriptionName("sensor-processor-sub")
                .subscriptionType(SubscriptionType.Exclusive)
                .subscribe();

        DataProcessor processor = new DataProcessor();

        System.out.println("Started processing messages...");

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

    @Override
    public Void process(byte[] input, Context context) throws Exception {
        try {
            SensorData data = mapper.readValue(input, SensorData.class);

            boolean isAnomaly = isAnomalous(data);

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "INSERT INTO sensor_data (sensor_id, temperature, humidity, timestamp, is_anomaly) " +
                        "VALUES (?, ?, ?, ?, ?)";

                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, data.getSensorId());
                stmt.setDouble(2, data.getTemperature());
                stmt.setDouble(3, data.getHumidity());
                stmt.setTimestamp(4, java.sql.Timestamp.valueOf(data.getTimestamp()));
                stmt.setBoolean(5, isAnomaly);

                stmt.executeUpdate();
                System.out.println("Stored data in PostgreSQL: " + mapper.writeValueAsString(data));
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