-- ─────────────────────────────────────────────────────────────────────────────
-- V2: Enable TimescaleDB and convert load_metrics to a hypertable
--
-- A hypertable transparently partitions data by the time column (sampled_at).
-- This makes time-range queries (last 5 minutes of load test data) very fast
-- even with millions of rows — each partition covers a configurable time window.
-- ─────────────────────────────────────────────────────────────────────────────

-- Enable the TimescaleDB extension. The image already has it installed;
-- we just need to activate it in this database.
CREATE EXTENSION IF NOT EXISTS timescaledb CASCADE;

-- Convert load_metrics into a hypertable partitioned by sampled_at.
-- chunk_time_interval = 1 hour means each partition covers 1 hour of data.
-- This is appropriate for load tests that typically run minutes to hours.
SELECT create_hypertable(
    'load_metrics',
    'sampled_at',
    chunk_time_interval => INTERVAL '1 hour',
    if_not_exists => TRUE
);

-- Continuous aggregate: pre-compute per-minute p99 for fast dashboard queries.
-- The Grafana dashboard queries this view instead of the raw hypertable.
CREATE MATERIALIZED VIEW load_metrics_per_minute
WITH (timescaledb.continuous) AS
SELECT
    test_run_id,
    time_bucket('1 minute', sampled_at) AS bucket,
    avg(rps)        AS avg_rps,
    avg(p50_ms)     AS avg_p50,
    avg(p95_ms)     AS avg_p95,
    max(p99_ms)     AS max_p99,
    avg(error_rate) AS avg_error_rate,
    sum(total_requests) AS total_requests
FROM load_metrics
GROUP BY test_run_id, bucket
WITH NO DATA;
