package io.github.shazaanashraff.apiforge.modules.reporter;

import io.github.shazaanashraff.apiforge.modules.executor.TestCaseResult;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class HtmlReportRenderer implements ReportRenderer {

  @Override
  public ReportFormat format() {
    return ReportFormat.HTML;
  }

  @Override
  public ReportOutput render(ReportRequest request) {
    var result = request.result();
    Map<String, List<TestCaseResult>> byCategory =
        result.results().stream().collect(Collectors.groupingBy(TestCaseResult::category));

    StringBuilder html = new StringBuilder();
    html.append("<!DOCTYPE html><html><head>")
        .append("<meta charset='UTF-8'>")
        .append("<title>APIForge Test Report</title>")
        .append("<style>body{font-family:sans-serif;margin:2em}")
        .append("table{border-collapse:collapse;width:100%}")
        .append("th,td{border:1px solid #ccc;padding:6px 10px;text-align:left}")
        .append("th{background:#f0f0f0}.pass{color:green}.fail{color:red}</style>")
        .append("</head><body>");

    html.append("<h1>APIForge Test Report</h1>");
    html.append("<p><strong>Run ID:</strong> ").append(escape(result.testRunId())).append("</p>");
    html.append("<p>")
        .append("<span class='pass'>Passed: ")
        .append(result.passed())
        .append("</span> &nbsp;|&nbsp; ")
        .append("<span class='fail'>Failed: ")
        .append(result.failed())
        .append("</span> &nbsp;|&nbsp; ")
        .append("Total: ")
        .append(result.results().size())
        .append(" &nbsp;|&nbsp; Duration: ")
        .append(result.finishedAt() - result.startedAt())
        .append("ms</p>");

    for (Map.Entry<String, List<TestCaseResult>> entry : byCategory.entrySet()) {
      html.append("<h2>").append(escape(entry.getKey())).append("</h2>");
      html.append("<table><tr><th>ID</th><th>Method</th><th>Path</th>")
          .append("<th>Status</th><th>Time (ms)</th><th>Result</th></tr>");
      for (TestCaseResult tc : entry.getValue()) {
        String resultCell =
            tc.passed()
                ? "<span class='pass'>PASS</span>"
                : "<span class='fail'>FAIL: " + escape(tc.failureReason()) + "</span>";
        html.append("<tr>")
            .append("<td>")
            .append(escape(tc.testCaseId()))
            .append("</td>")
            .append("<td>")
            .append(escape(tc.httpMethod()))
            .append("</td>")
            .append("<td>")
            .append(escape(tc.endpointPath()))
            .append("</td>")
            .append("<td>")
            .append(tc.statusCode())
            .append("</td>")
            .append("<td>")
            .append(tc.responseTimeMs())
            .append("</td>")
            .append("<td>")
            .append(resultCell)
            .append("</td>")
            .append("</tr>");
      }
      html.append("</table>");
    }

    html.append("</body></html>");
    return new ReportOutput(ReportFormat.HTML, html.toString());
  }

  private static String escape(String s) {
    if (s == null) return "";
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }
}
