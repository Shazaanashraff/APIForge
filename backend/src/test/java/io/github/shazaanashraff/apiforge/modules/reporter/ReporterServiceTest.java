package io.github.shazaanashraff.apiforge.modules.reporter;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.shazaanashraff.apiforge.modules.executor.ExecutionResult;
import io.github.shazaanashraff.apiforge.modules.executor.TestCaseResult;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ReporterServiceTest {

  private final ReporterService service = new ReporterService();

  private static ExecutionResult minimalResult() {
    TestCaseResult tc =
        new TestCaseResult(
            "tc-1", "/test", "GET", "HAPPY_PATH", 200, 10L, Map.of(), "{}", true, null);
    return new ExecutionResult("run-1", List.of(tc), 1, 0, 0, 100L, 200L);
  }

  @Test
  void generateHtmlReturnsHtmlFormat() {
    ReportOutput output =
        service.generate(new ReportRequest(minimalResult(), List.of(), ReportFormat.HTML));
    assertThat(output.format()).isEqualTo(ReportFormat.HTML);
    assertThat(output.content()).contains("<!DOCTYPE html>");
  }

  @Test
  void generateJsonReturnsJsonFormat() {
    ReportOutput output =
        service.generate(new ReportRequest(minimalResult(), List.of(), ReportFormat.JSON));
    assertThat(output.format()).isEqualTo(ReportFormat.JSON);
    assertThat(output.content()).contains("testRunId");
  }

  @Test
  void generateJunitXmlReturnsXmlFormat() {
    ReportOutput output =
        service.generate(new ReportRequest(minimalResult(), List.of(), ReportFormat.JUNIT_XML));
    assertThat(output.format()).isEqualTo(ReportFormat.JUNIT_XML);
    assertThat(output.content()).contains("<testsuite");
  }
}
