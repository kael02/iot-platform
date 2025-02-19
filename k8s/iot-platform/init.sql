CREATE TABLE sensor_data (
    id UInt64,
    sensor_id String,
    temperature Float64,
    humidity Float64,
    timestamp DateTime,
    is_anomaly UInt8
)
ENGINE = MergeTree()
ORDER BY (id);
