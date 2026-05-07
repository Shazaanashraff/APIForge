package io.github.shazaanashraff.apiforge.modules.kafka;

/**
 * Fired after each individual test case is executed and validated. Consumers: reporter (to persist
 * the result row), SSE layer (real-time progress).
 *
 * <p>Avro schema: {@code src/main/avro/TestCaseCompletedEvent.avsc}
 */
public record TestCaseCompletedEvent(
    String testRunId,
    String testCaseId,
    String endpointPath,
    String httpMethod,
    String category,
    int statusCode,
    long responseTimeMs,
    boolean passed,
    String failureReason,
    long completedAt) {}
