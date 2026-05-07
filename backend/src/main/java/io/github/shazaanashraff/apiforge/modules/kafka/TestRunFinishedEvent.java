package io.github.shazaanashraff.apiforge.modules.kafka;

/**
 * Fired when all test cases in a run have been executed. Consumers: reporter (to finalise the run
 * record), SSE layer.
 *
 * <p>Avro schema: {@code src/main/avro/TestRunFinishedEvent.avsc}
 */
public record TestRunFinishedEvent(
    String testRunId,
    String projectId,
    String tenantId,
    int totalTestCases,
    int passed,
    int failed,
    int skipped,
    long finishedAt) {}
