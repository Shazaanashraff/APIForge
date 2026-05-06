package io.github.shazaanashraff.apiforge.modules.loadtester;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LoadMetricRepository extends JpaRepository<LoadMetric, UUID> {

  List<LoadMetric> findByTestRunIdOrderBySampledAtAsc(UUID testRunId);

  /**
   * Returns per-minute bucketed metrics for a test run using the TimescaleDB continuous aggregate.
   * This is much faster than querying the raw hypertable for large load tests.
   *
   * <p>The view load_metrics_per_minute is defined in V2 migration.
   */
  @Query(
      value =
          """
          SELECT bucket, avg_rps, avg_p50, avg_p95, max_p99, avg_error_rate, total_requests
          FROM load_metrics_per_minute
          WHERE test_run_id = :testRunId
            AND bucket >= :from AND bucket <= :to
          ORDER BY bucket ASC
          """,
      nativeQuery = true)
  List<Object[]> findBucketedMetrics(UUID testRunId, Instant from, Instant to);
}
