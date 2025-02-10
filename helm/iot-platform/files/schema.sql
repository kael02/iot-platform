CREATE TABLE sensor_data (
                             id SERIAL PRIMARY KEY,
                             sensor_id VARCHAR(50),
                             temperature DOUBLE PRECISION,
                             humidity DOUBLE PRECISION,
                             timestamp TIMESTAMP,
                             is_anomaly BOOLEAN
);