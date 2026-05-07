package io.github.shazaanashraff.apiforge.modules.reporter;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.shazaanashraff.apiforge.modules.executor.ExecutionResult;
import io.github.shazaanashraff.apiforge.modules.executor.TestCaseResult;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JsonReportRendererTest {

  private static ExecutionResult twoResults() {
    TestCaseResult pass =
        new TestCaseResult(
            "tc-1", "/pets", "GET", "HAPPY_PATH", 200, 40L, Map.of(), "{}", true, null);
    TestCaseResult fail =
        new TestCaseResult(
            "tc-2",
            "/pets",
            "POST",
            "NEGATIVE",
            422,
            60L,
            Map.of(),
            "{}",
            false,
            "Expected 400 but got 422");
    return new ExecutionResult("run-42", List.of(pass, fail), 1, 1, 0, 1000L, 3000L);
  }

  private final JsonReportRenderer renderer = new JsonReportRenderer();

  @Test
  void jsonContainsTestRunId() {
    ReportRequest request = new ReportRequest(twoResults(), List.of(), ReportFormat.JSON);
    assertThat(renderer.render(request).content()).contains("run-42");
  }

  @Test
  void jsonContainsPassedAndFailedCounts() {
    ReportRequest request = new ReportRequest(twoResults(), List.of(), ReportFormat.JSON);
    String json = renderer.render(request).content();
    assertThat(json).contains("\"passed\" : 1").contains("\"failed\" : 1");
  }

  @Test
  void jsonIsValidJsonWithResultsArray() {
    ReportRequest request = new ReportRequest(twoResults(), List.of(), ReportFormat.JSON);
    String json = renderer.render(request).content();
    assertThat(json).startsWith("{").endsWith("}");
    assertThat(json).contains("\"results\"");
  }
}
