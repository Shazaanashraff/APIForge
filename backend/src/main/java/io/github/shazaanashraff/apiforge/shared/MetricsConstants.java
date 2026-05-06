package io.github.shazaanashraff.apiforge.shared;

/**
 * Canonical metric names for all APIForge custom counters, timers, and gauges.
 *
 * <p>Using constants avoids typos across modules and makes it easy to find every metric
 * in a single grep. Follow the {@code apiforge.<module>.<noun>} naming convention.
 * All metrics are automatically tagged with {@code application} and {@code environment}
 * by {@link ObservabilityConfig}.
 */
public final class MetricsConstants {

  private MetricsConstants() {}

  // ── Schema Parser (S05) ────────────────────────────────────────────────────
  public static final String SCHEMA_PARSE_TOTAL = "apiforge.schema.parse.total";
  public static final String SCHEMA_PARSE_DURATION = "apiforge.schema.parse.duration";
  public static final String SCHEMA_ENDPOINTS_COUNT = "apiforge.schema.endpoints.count";

  // ── Test Case Generator (S07) ──────────────────────────────────────────────
  public static final String TESTGEN_CASES_GENERATED = "apiforge.testgen.cases.generated";
  public static final String TESTGEN_DURATION = "apiforge.testgen.duration";

  // ── Executor (S10) ────────────────────────────────────────────────────────
  public static final String EXECUTOR_REQUESTS_SENT = "apiforge.executor.requests.sent";
  public static final String EXECUTOR_REQUESTS_FAILED = "apiforge.executor.requests.failed";
  public static final String EXECUTOR_DURATION = "apiforge.executor.duration";

  // ── Load Tester (S12) ─────────────────────────────────────────────────────
  public static final String LOAD_VUS_ACTIVE = "apiforge.load.vus.active";
  public static final String LOAD_RPS = "apiforge.load.rps";

  // ── Tag keys ──────────────────────────────────────────────────────────────
  public static final String TAG_TENANT_ID = "tenantId";
  public static final String TAG_PROJECT_ID = "projectId";
  public static final String TAG_STATUS = "status";
  public static final String TAG_SPEC_FORMAT = "specFormat";
}
