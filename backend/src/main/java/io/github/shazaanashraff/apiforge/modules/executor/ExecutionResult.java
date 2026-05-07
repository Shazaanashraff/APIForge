package io.github.shazaanashraff.apiforge.modules.executor;

import java.util.List;

/** Aggregated result for a full test-run execution. */
public record ExecutionResult(
    String testRunId,
    List<TestCaseResult> results,
    int passed,
    int failed,
    int skipped,
    long startedAt,
    long finishedAt) {}
