package io.github.shazaanashraff.apiforge.modules.reporter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.shazaanashraff.apiforge.modules.executor.ExecutionResult;
import java.util.LinkedHashMap;
import java.util.Map;

class JsonReportRenderer implements ReportRenderer {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Override
  public ReportFormat format() {
    return ReportFormat.JSON;
  }

  @Override
  public ReportOutput render(ReportRequest request) {
    ExecutionResult result = request.result();
    Map<String, Object> report = new LinkedHashMap<>();
    report.put("testRunId", result.testRunId());
    report.put("passed", result.passed());
    report.put("failed", result.failed());
    report.put("skipped", result.skipped());
    report.put("total", result.results().size());
    report.put("durationMs", result.finishedAt() - result.startedAt());
    report.put("results", result.results());
    if (request.validations() != null && !request.validations().isEmpty()) {
      report.put("validations", request.validations());
    }
    try {
      return new ReportOutput(
          ReportFormat.JSON, MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(report));
    } catch (Exception e) {
      return new ReportOutput(ReportFormat.JSON, "{\"error\":\"" + e.getMessage() + "\"}");
    }
  }
}
