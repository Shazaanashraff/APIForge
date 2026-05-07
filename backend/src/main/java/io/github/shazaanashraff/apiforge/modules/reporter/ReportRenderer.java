package io.github.shazaanashraff.apiforge.modules.reporter;

interface ReportRenderer {
  ReportFormat format();

  ReportOutput render(ReportRequest request);
}
