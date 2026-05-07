package io.github.shazaanashraff.apiforge.modules.executor;

/** Configuration that controls how the executor runs a test suite. */
public record ExecutionConfig(
    int concurrency, long timeoutMs, int retryMaxAttempts, long retryBackoffMs) {

  /** Sane defaults drawn from {@code application.yml apiforge.executor.*} properties. */
  public static ExecutionConfig defaults() {
    return new ExecutionConfig(10, 30_000L, 3, 1_000L);
  }
}
