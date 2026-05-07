package io.github.shazaanashraff.apiforge.modules.loadtester;

import java.util.Map;

public record LoadScenario(
    String testRunId,
    String projectId,
    String tenantId,
    String baseUrl,
    String endpointPath,
    String httpMethod,
    int virtualUsers,
    int rampUpSeconds,
    int durationSeconds,
    Object requestBody,
    Map<String, String> headers) {

  public static LoadScenario of(
      String testRunId,
      String projectId,
      String tenantId,
      String baseUrl,
      String endpointPath,
      String httpMethod,
      int virtualUsers,
      int durationSeconds) {
    return new LoadScenario(
        testRunId,
        projectId,
        tenantId,
        baseUrl,
        endpointPath,
        httpMethod,
        virtualUsers,
        0,
        durationSeconds,
        null,
        null);
  }
}
