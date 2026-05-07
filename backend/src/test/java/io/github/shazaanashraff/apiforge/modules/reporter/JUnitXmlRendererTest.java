package io.github.shazaanashraff.apiforge.modules.reporter;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.shazaanashraff.apiforge.modules.executor.ExecutionResult;
import io.github.shazaanashraff.apiforge.modules.executor.TestCaseResult;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JUnitXmlRendererTest {

  private static ExecutionResult results(boolean passed) {
    TestCaseResult tc =
        new TestCaseResult(
            "tc-1",
            "/items",
            "GET",
            "HAPPY_PATH",
            200,
            80L,
            Map.of(),
            "{}",
            passed,
            passed ? null : "Expected 200 but got 500");
    return new ExecutionResult("run-1", List.of(tc), passed ? 1 : 0, passed ? 0 : 1, 0, 0L, 500L);
  }

  private final JUnitXmlRenderer renderer = new JUnitXmlRenderer();

  @Test
  void xmlStartsWithDeclarationAndTestsuiteRoot() {
    ReportRequest request = new ReportRequest(results(true), List.of(), ReportFormat.JUNIT_XML);
    String xml = renderer.render(request).content();
    assertThat(xml).startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    assertThat(xml).contains("<testsuite");
  }

  @Test
  void passingTestCaseUsesShortClosingTag() {
    ReportRequest request = new ReportRequest(results(true), List.of(), ReportFormat.JUNIT_XML);
    String xml = renderer.render(request).content();
    assertThat(xml).contains("<testcase").contains("/>");
    assertThat(xml).doesNotContain("<failure");
  }

  @Test
  void failedTestCaseHasFailureElement() {
    ReportRequest request = new ReportRequest(results(false), List.of(), ReportFormat.JUNIT_XML);
    String xml = renderer.render(request).content();
    assertThat(xml).contains("<failure");
    assertThat(xml).contains("Expected 200 but got 500");
  }
}
