package io.github.shazaanashraff.apiforge.modules.testgenerator;

import io.github.shazaanashraff.apiforge.modules.schemaparser.Endpoint;
import io.github.shazaanashraff.apiforge.modules.schemaparser.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpMethod;

class PayloadSizeGenerator implements TestCaseGenerator {

  private static final int ONE_MB = 1024 * 1024;
  private static final int TEN_MB = 10 * ONE_MB;

  @Override
  public TestCategory category() {
    return TestCategory.PAYLOAD_SIZE;
  }

  @Override
  public List<TestCase> generate(TestGenerationContext ctx) {
    Endpoint ep = ctx.endpoint();
    if (ep.method() != HttpMethod.POST && ep.method() != HttpMethod.PUT) return List.of();
    if (ep.requestBody() == null) return List.of();

    List<TestCase> cases = new ArrayList<>();
    Map<String, String> pathParams = buildPathParams(ep);
    boolean hasDeclaredMax = ep.payloadSizeHint() != null && ep.payloadSizeHint().hasDeclaredMax();

    // 1. Empty body
    cases.add(
        sizeCase(
            ep, pathParams, Map.of(), "empty body", List.of(Assertion.statusCode(400))));

    if (hasDeclaredMax) {
      long maxBytes = ep.payloadSizeHint().declaredMaxBytes();

      // 2. Just under max (valid)
      String justUnder = "x".repeat((int) Math.max(1, maxBytes - 1));
      cases.add(
          sizeCase(
              ep,
              pathParams,
              Map.of("data", justUnder),
              "just under max (" + (maxBytes - 1) + " bytes)",
              List.of(Assertion.statusCodeRange("2xx"))));

      // 3. Just over max (→413/400)
      String justOver = "x".repeat((int) (maxBytes + 1));
      cases.add(
          sizeCase(
              ep,
              pathParams,
              Map.of("data", justOver),
              "just over max (" + (maxBytes + 1) + " bytes)",
              List.of(Assertion.statusCodeRange("4xx"))));

      // 4. 10× max
      int tenXSize = (int) Math.min(maxBytes * 10, TEN_MB);
      cases.add(
          sizeCase(
              ep,
              pathParams,
              Map.of("data", "x".repeat(tenXSize)),
              "10x max (" + (maxBytes * 10) + " bytes)",
              List.of(Assertion.statusCodeRange("4xx"))));
    } else {
      // No declared max: test 1MB and 10MB
      cases.add(
          sizeCase(
              ep,
              pathParams,
              Map.of("data", "x".repeat(ONE_MB)),
              "1MB payload",
              List.of(Assertion.statusCodeRange("any"))));
      cases.add(
          sizeCase(
              ep,
              pathParams,
              Map.of("data", "x".repeat(TEN_MB)),
              "10MB payload",
              List.of(Assertion.statusCodeRange("any"))));
    }

    return cases;
  }

  private TestCase sizeCase(
      Endpoint ep,
      Map<String, String> pathParams,
      Object body,
      String label,
      List<Assertion> assertions) {
    return new TestCase(
        UUID.randomUUID().toString(),
        TestCategory.PAYLOAD_SIZE,
        ep.path(),
        ep.method(),
        Map.of(),
        Map.of(),
        pathParams,
        body,
        assertions,
        "Payload size: " + label + " for " + ep.method() + " " + ep.path(),
        false);
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
