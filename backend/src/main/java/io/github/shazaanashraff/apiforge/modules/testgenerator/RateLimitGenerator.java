package io.github.shazaanashraff.apiforge.modules.testgenerator;

import io.github.shazaanashraff.apiforge.modules.schemaparser.Endpoint;
import io.github.shazaanashraff.apiforge.modules.schemaparser.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class RateLimitGenerator implements TestCaseGenerator {

  @Override
  public TestCategory category() {
    return TestCategory.RATE_LIMIT;
  }

  @Override
  public List<TestCase> generate(TestGenerationContext ctx) {
    Endpoint ep = ctx.endpoint();
    Map<String, String> pathParams = buildPathParams(ep, ctx);
    Object body =
        ep.requestBody() != null ? ctx.dataGenerator().generatePayload(ep.requestBody()) : null;
    int burst = ctx.rateLimitBurstCount();

    List<TestCase> cases = new ArrayList<>(burst);
    for (int i = 0; i < burst; i++) {
      cases.add(
          new TestCase(
              UUID.randomUUID().toString(),
              TestCategory.RATE_LIMIT,
              ep.path(),
              ep.method(),
              Map.of(),
              Map.of(),
              pathParams,
              body,
              List.of(Assertion.statusCodeRange("2xx")),
              "Rate-limit burst #" + (i + 1) + "/" + burst + " — at least one → 429",
              false));
    }
    return cases;
  }

  private Map<String, String> buildPathParams(Endpoint ep, TestGenerationContext ctx) {
    Map<String, String> params = new HashMap<>();
    if (ep.parameters() == null) return params;
    for (Parameter p : ep.parameters()) {
      if ("path".equals(p.in()))
        params.put(p.name(), ctx.dataGenerator().objectIds().validObjectId());
    }
    return params;
  }
}
