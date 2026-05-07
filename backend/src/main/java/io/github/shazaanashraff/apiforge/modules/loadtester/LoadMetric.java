package io.github.shazaanashraff.apiforge.modules.loadtester;

import io.github.shazaanashraff.apiforge.shared.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single time-series sample from a load test run.
 *
 * <p>The load tester (S12) samples metrics every second and writes rows here. The underlying
 * Postgres table is a TimescaleDB hypertable partitioned by sampled_at (V2 migration), which makes
 * time-range queries on this data extremely fast.
 *
 * <p>The Grafana dashboard queries the load_metrics_per_minute continuous aggregate view (also
 * defined in V2) rather than this table directly.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "load_metrics")
public class LoadMetric extends BaseEntity {

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "test_run_id", nullable = false)
  private UUID testRunId;

  /** The timestamp this sample was taken. TimescaleDB partitions by this column. */
  @Column(name = "sampled_at", nullable = false)
  private Instant sampledAt;

  /** Requests per second at this sample point. */
  @Column(name = "rps", precision = 10, scale = 2)
  private BigDecimal rps;

  /** Number of virtual users active when this sample was taken. */
  @Column(name = "active_vus")
  private Integer activeVus;

  @Column(name = "p50_ms")
  private Long p50Ms;

  @Column(name = "p95_ms")
  private Long p95Ms;

  @Column(name = "p99_ms")
  private Long p99Ms;

  @Column(name = "max_ms")
  private Long maxMs;

  /** Error rate from 0.0 (no errors) to 1.0 (all errors). */
  @Column(name = "error_rate", precision = 5, scale = 4)
  private BigDecimal errorRate;

  @Column(name = "total_requests")
  private Long totalRequests;
}
