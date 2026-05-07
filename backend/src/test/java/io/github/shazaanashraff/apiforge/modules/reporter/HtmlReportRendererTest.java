package io.github.shazaanashraff.apiforge.modules.reporter;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.shazaanashraff.apiforge.modules.executor.ExecutionResult;
import io.github.shazaanashraff.apiforge.modules.executor.TestCaseResult;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class HtmlReportRendererTest {

  private static ExecutionResult singleResult(boolean passed, String category) {
    TestCaseResult tc =
        new TestCaseResult(
            "tc-1",
            "/pets",
            "GET",
            category,
            200,
            50L,
            Map.of(),
            "{}",
            passed,
            passed ? null : "Expected 200 but got 404");
    return new ExecutionResult(
        "run-1", List.of(tc), passed ? 1 : 0, passed ? 0 : 1, 0, 1000L, 2000L);
  }

  private final HtmlReportRenderer renderer = new HtmlReportRenderer();

  @Test
  void htmlContainsTestRunId() {
    ReportRequest request =
        new ReportRequest(singleResult(true, "HAPPY_PATH"), List.of(), ReportFormat.HTML);
    String content = renderer.render(request).content();
    assertThat(content).contains("run-1");
  }

  @Test
  void htmlGroupsResultsByCategory() {
    ReportRequest request =
        new ReportRequest(singleResult(true, "HAPPY_PATH"), List.of(), ReportFormat.HTML);
    String content = renderer.render(request).content();
    assertThat(content).contains("HAPPY_PATH");
  }

  @Test
  void failedTestCaseContainsFailureReason() {
    ReportRequest request =
        new ReportRequest(singleResult(false, "NEGATIVE"), List.of(), ReportFormat.HTML);
    String content = renderer.render(request).content();
    assertThat(content).contains("Expected 200 but got 404");
  }
}
