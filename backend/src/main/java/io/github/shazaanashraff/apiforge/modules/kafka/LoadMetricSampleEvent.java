package io.github.shazaanashraff.apiforge.modules.kafka;

/**
 * One second of load-test telemetry, emitted continuously during a load run. Consumers: load-tester
 * module (writes to TimescaleDB hypertable), Grafana live dashboard.
 *
 * <p>Avro schema: {@code src/main/avro/LoadMetricSampleEvent.avsc}
 */
public record LoadMetricSampleEvent(
    String testRunId,
    long timestamp,
    int virtualUsers,
    double requestsPerSecond,
    double errorRate,
    long p50Ms,
    long p95Ms,
    long p99Ms) {}
