package com.iotplatform.producer;

import com.iotplatform.producer.SensorData;
import org.apache.pulsar.client.api.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class DataProducer {
    // private static final String SERVICE_URL = System.getenv().getOrDefault("PULSAR_URL", "pulsar://pulsar:6650");

private static final String SERVICE_URL = "pulsar://pulsar-proxy.pulsar.svc.cluster.local:6650";


    private static final String TOPIC_NAME = "persistent://public/default/sensor-data";

    public static void main(String[] args) throws Exception {
        PulsarClient client = null;
        int maxRetries = 5;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                client = PulsarClient.builder()
                        .serviceUrl(SERVICE_URL)
                        .build();
                break;
            } catch (Exception e) {
                retryCount++;
                if (retryCount == maxRetries) {
                    throw e;
                }
                Thread.sleep(5000);
            }
        }

        Producer<String> producer = client.newProducer(Schema.STRING)
                .topic(TOPIC_NAME)
                .create();

        ObjectMapper mapper = new ObjectMapper();
        Random random = new Random();

        while (true) {
            try {
                String timestampMillis = String.valueOf(System.currentTimeMillis());

                long millis = Long.parseLong(timestampMillis);
                LocalDateTime dateTime = Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");
                String formattedTime = dateTime.format(formatter);

                SensorData data = new SensorData(
                        "sensor-" + random.nextInt(5),
                        random.nextDouble() * 100,
                        random.nextDouble() * 50,
                        formattedTime
                );

                String json = mapper.writeValueAsString(data);
                producer.send(json);
                System.out.println("Sent: " + json);
                Thread.sleep(1000);
            } catch (Exception e) {
                System.err.println("Error sending message: " + e.getMessage());
                Thread.sleep(5000);
            }
        }
    }
}