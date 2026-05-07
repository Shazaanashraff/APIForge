package io.github.shazaanashraff.apiforge.modules.executor;

import io.github.shazaanashraff.apiforge.shared.BaseEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One test run = one execution of a test suite against the target API.
 *
 * <p>Lifecycle: PENDING → RUNNING → COMPLETED or FAILED. Real-time progress is streamed via SSE
 * (S15); final results stored in test_results.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "test_runs")
public class TestRun extends BaseEntity {

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "project_id", nullable = false)
  private UUID projectId;

  @Column(name = "api_spec_id")
  private UUID apiSpecId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private TestRunStatus status = TestRunStatus.PENDING;

  /** Postgres text[] column — the categories the user selected for this run. */
  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "categories_selected", columnDefinition = "text[]")
  private String[] categoriesSelected;

  @Column(name = "concurrency", nullable = false)
  private int concurrency = 1;

  @Column(name = "base_url", length = 2048)
  private String baseUrl;

  @Column(name = "total_cases")
  private Integer totalCases;

  @Column(name = "passed_cases")
  private Integer passedCases;

  @Column(name = "failed_cases")
  private Integer failedCases;

  @Column(name = "skipped_cases")
  private Integer skippedCases;

  @Column(name = "started_at")
  private Instant startedAt;

  @Column(name = "completed_at")
  private Instant completedAt;

  public enum TestRunStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED
  }
}
