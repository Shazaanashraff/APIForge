package io.github.shazaanashraff.apiforge.modules.testgenerator;

import io.github.shazaanashraff.apiforge.modules.schemaparser.Endpoint;
import io.github.shazaanashraff.apiforge.modules.schemaparser.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class PerformanceSlaGenerator implements TestCaseGenerator {

  @Override
  public TestCategory category() {
    return TestCategory.PERFORMANCE_SLA;
  }

  @Override
  public List<TestCase> generate(TestGenerationContext ctx) {
    Endpoint ep = ctx.endpoint();
    long threshold =
        (ep.slaHint() != null && ep.slaHint().hasSla())
            ? ep.slaHint().thresholdMs()
            : ctx.defaultSlaThresholdMs();

    return List.of(
        new TestCase(
            UUID.randomUUID().toString(),
            TestCategory.PERFORMANCE_SLA,
            ep.path(),
            ep.method(),
            Map.of(),
            Map.of(),
            buildPathParams(ep),
            null,
            List.of(Assertion.statusCodeRange("2xx"), Assertion.responseTimeBelow(threshold)),
            "Performance: " + ep.method() + " " + ep.path() + " must respond in < " + threshold + "ms",
            false));
  }

  private Map<String, String> buildPathParams(Endpoint ep) {
    Map<String, String> params = new HashMap<>();
    if (ep.parameters() == null) return params;
    for (Parameter p : ep.parameters()) {
      if ("path".equals(p.in())) params.put(p.name(), "test-value");
    }
    return params;
  }
}
