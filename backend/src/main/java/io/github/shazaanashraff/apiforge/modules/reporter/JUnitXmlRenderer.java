package io.github.shazaanashraff.apiforge.modules.reporter;

import io.github.shazaanashraff.apiforge.modules.executor.TestCaseResult;

class JUnitXmlRenderer implements ReportRenderer {

  @Override
  public ReportFormat format() {
    return ReportFormat.JUNIT_XML;
  }

  @Override
  public ReportOutput render(ReportRequest request) {
    var result = request.result();
    long totalMs = result.finishedAt() - result.startedAt();

    StringBuilder xml = new StringBuilder();
    xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    xml.append("<testsuite name=\"APIForge\"")
        .append(" tests=\"")
        .append(result.results().size())
        .append("\"")
        .append(" failures=\"")
        .append(result.failed())
        .append("\"")
        .append(" errors=\"0\"")
        .append(" time=\"")
        .append(String.format("%.3f", totalMs / 1000.0))
        .append("\"")
        .append(">\n");

    for (TestCaseResult tc : result.results()) {
      String name = escape(tc.httpMethod() + " " + tc.endpointPath() + " - " + tc.category());
      xml.append("  <testcase")
          .append(" name=\"")
          .append(name)
          .append("\"")
          .append(" classname=\"")
          .append(escape(tc.category()))
          .append("\"")
          .append(" time=\"")
          .append(String.format("%.3f", tc.responseTimeMs() / 1000.0))
          .append("\"");

      if (tc.passed()) {
        xml.append("/>\n");
      } else {
        xml.append(">\n")
            .append("    <failure message=\"")
            .append(escape(tc.failureReason()))
            .append("\"/>\n")
            .append("  </testcase>\n");
      }
    }

    xml.append("</testsuite>");
    return new ReportOutput(ReportFormat.JUNIT_XML, xml.toString());
  }

  private static String escape(String s) {
    if (s == null) return "";
    return s.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;");
  }
}
