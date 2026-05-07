package io.github.shazaanashraff.apiforge.modules.kafka;

/**
 * Fired when a test run begins. Consumers: reporter (to initialise the run record), SSE layer.
 *
 * <p>Avro schema: {@code src/main/avro/TestRunStartedEvent.avsc}
 */
public record TestRunStartedEvent(
    String testRunId,
    String projectId,
    String tenantId,
    String specName,
    long startedAt,
    int totalTestCases) {}
