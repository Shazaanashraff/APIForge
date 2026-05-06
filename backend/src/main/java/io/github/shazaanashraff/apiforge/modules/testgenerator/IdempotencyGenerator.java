package io.github.shazaanashraff.apiforge.modules.testgenerator;

import io.github.shazaanashraff.apiforge.modules.schemaparser.Endpoint;
import io.github.shazaanashraff.apiforge.modules.schemaparser.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpMethod;

class IdempotencyGenerator implements TestCaseGenerator {

  @Override
  public TestCategory category() {
    return TestCategory.IDEMPOTENCY;
  }

  @Override
  public List<TestCase> generate(TestGenerationContext ctx) {
    Endpoint ep = ctx.endpoint();
    if (ep.method() != HttpMethod.PUT && ep.method() != HttpMethod.DELETE) {
      return List.of();
    }

    Map<String, String> pathParams = buildPathParams(ep);
    Object body = ep.requestBody() != null ? ctx.dataGenerator().generatePayload(ep.requestBody()) : null;
    String desc = "Idempotency: " + ep.method() + " " + ep.path() + " run %s — expect same result";

    return List.of(
        new TestCase(
            UUID.randomUUID().toString(),
            TestCategory.IDEMPOTENCY,
            ep.path(),
            ep.method(),
            Map.of(),
            Map.of(),
            pathParams,
            body,
            List.of(Assertion.statusCodeRange("2xx")),
            String.format(desc, "#1"),
            false),
        new TestCase(
            UUID.randomUUID().toString(),
            TestCategory.IDEMPOTENCY,
            ep.path(),
            ep.method(),
            Map.of(),
            Map.of(),
            pathParams,
            body,
            List.of(Assertion.statusCodeRange("2xx")),
            String.format(desc, "#2"),
            false));
  }

  private Map<String, String> buildPathParams(Endpoint ep) {
    Map<String, String> params = new HashMap<>();
    if (ep.parameters() == null) return params;
    for (Parameter p : ep.parameters()) {
      if ("path".equals(p.in())) params.put(p.name(), "idempotent-test-id");
    }
    return params;
  }
}
