package io.github.shazaanashraff.apiforge.modules.reporter;

import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ReporterService {

  private final Map<ReportFormat, ReportRenderer> renderers = new EnumMap<>(ReportFormat.class);

  public ReporterService() {
    renderers.put(ReportFormat.HTML, new HtmlReportRenderer());
    renderers.put(ReportFormat.JSON, new JsonReportRenderer());
    renderers.put(ReportFormat.JUNIT_XML, new JUnitXmlRenderer());
  }

  public ReportOutput generate(ReportRequest request) {
    ReportRenderer renderer = renderers.get(request.format());
    if (renderer == null) {
      throw new IllegalArgumentException("Unsupported report format: " + request.format());
    }
    return renderer.render(request);
  }
}
