package io.github.shazaanashraff.apiforge.modules.executor;

import io.github.shazaanashraff.apiforge.shared.BaseEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The outcome of executing a single test case.
 *
 * <p>Created by the executor (S10) after each HTTP call. The reporter (S13) reads these to
 * build the HTML/JSON/JUnit XML report.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "test_results")
public class TestResult extends BaseEntity {

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "test_run_id", nullable = false)
  private UUID testRunId;

  @Column(name = "test_case_id", nullable = false)
  private UUID testCaseId;

  @Column(name = "passed", nullable = false)
  private boolean passed;

  @Column(name = "actual_status_code")
  private Integer actualStatusCode;

  /** How long the HTTP round-trip took, in milliseconds. */
  @Column(name = "response_time_ms")
  private Long responseTimeMs;

  @Column(name = "response_headers", columnDefinition = "jsonb")
  private String responseHeaders;

  @Column(name = "response_body", columnDefinition = "TEXT")
  private String responseBody;

  /** Human-readable reason why the test failed, e.g. "Expected 201 but got 200". */
  @Column(name = "failure_reason", columnDefinition = "TEXT")
  private String failureReason;

  /** JSON array of per-assertion outcomes (status, schema, SLA, etc.). */
  @Column(name = "assertion_results", columnDefinition = "jsonb")
  private String assertionResults;

  @Column(name = "executed_at", nullable = false)
  private Instant executedAt = Instant.now();
}
