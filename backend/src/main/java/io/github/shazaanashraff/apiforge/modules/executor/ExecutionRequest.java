package io.github.shazaanashraff.apiforge.modules.executor;

import java.util.List;

/**
 * Input to {@link TestExecutorService}: the test cases to run plus runtime context.
 *
 * <p>Uses the fully qualified name for {@code testgenerator.TestCase} to avoid the naming conflict
 * with {@code executor.TestCase} (the JPA entity in this package).
 */
public record ExecutionRequest(
    List<io.github.shazaanashraff.apiforge.modules.testgenerator.TestCase> testCases,
    String baseUrl,
    String testRunId,
    String projectId,
    String tenantId,
    String authToken,
    ExecutionConfig config) {

  /** Convenience factory with defaults and no auth token. */
  public static ExecutionRequest of(
      List<io.github.shazaanashraff.apiforge.modules.testgenerator.TestCase> testCases,
      String baseUrl,
      String testRunId,
      String projectId,
      String tenantId) {
    return new ExecutionRequest(
        testCases, baseUrl, testRunId, projectId, tenantId, null, ExecutionConfig.defaults());
  }
}
