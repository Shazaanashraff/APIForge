package io.github.shazaanashraff.apiforge.modules.api;

public record RunTestsRequest(
    String specUrl,
    String baseUrl,
    String testRunId,
    String projectId,
    String tenantId,
    String authToken) {}
