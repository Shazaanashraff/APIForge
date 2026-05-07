package io.github.shazaanashraff.apiforge.modules.executor;

import java.util.Map;

/** The outcome of executing a single test case against the target API. */
public record TestCaseResult(
    String testCaseId,
    String endpointPath,
    String httpMethod,
    String category,
    int statusCode,
    long responseTimeMs,
    Map<String, String> responseHeaders,
    String responseBody,
    boolean passed,
    String failureReason) {}
