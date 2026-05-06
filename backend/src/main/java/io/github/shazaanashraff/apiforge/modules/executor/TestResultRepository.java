package io.github.shazaanashraff.apiforge.modules.executor;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TestResultRepository extends JpaRepository<TestResult, UUID> {

  List<TestResult> findByTestRunId(UUID testRunId);

  List<TestResult> findByTestRunIdAndPassed(UUID testRunId, boolean passed);

  /**
   * Returns counts grouped by category for the report summary.
   * Each row is [category, passCount, failCount].
   */
  @Query(
      """
      SELECT tc.category, SUM(CASE WHEN tr.passed THEN 1 ELSE 0 END), COUNT(tr)
      FROM TestResult tr
      JOIN TestCase tc ON tc.id = tr.testCaseId
      WHERE tr.testRunId = :testRunId
      GROUP BY tc.category
      """)
  List<Object[]> countByCategory(UUID testRunId);
}
