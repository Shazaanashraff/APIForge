package io.github.shazaanashraff.apiforge.modules.loadtester;

public record LoadTestResult(
    String testRunId,
    long totalRequests,
    long errorCount,
    double errorRate,
    long p50Ms,
    long p95Ms,
    long p99Ms,
    long maxMs,
    long durationMs) {}
