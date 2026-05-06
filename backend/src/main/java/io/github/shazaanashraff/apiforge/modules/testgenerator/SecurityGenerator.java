package io.github.shazaanashraff.apiforge.modules.testgenerator;

import io.github.shazaanashraff.apiforge.modules.schemaparser.Endpoint;
import io.github.shazaanashraff.apiforge.modules.schemaparser.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class SecurityGenerator implements TestCaseGenerator {

  @Override
  public TestCategory category() {
    return TestCategory.SECURITY;
  }

  @Override
  public List<TestCase> generate(TestGenerationContext ctx) {
    Endpoint ep = ctx.endpoint();
    List<TestCase> cases = new ArrayList<>();

    for (Parameter p : safeParams(ep)) {
      if ("query".equals(p.in())) {
        for (String payload : SecurityPayloads.SQL_INJECTION) {
          cases.add(securityCase(ep, p, payload, "SQL injection"));
        }
        for (String payload : SecurityPayloads.XSS) {
          cases.add(securityCase(ep, p, payload, "XSS"));
        }
        for (String payload : SecurityPayloads.NOSQL_INJECTION_QUERY_PARAM) {
          cases.add(securityCase(ep, p, payload, "NoSQL injection (query)"));
        }
      }
    }

    for (Parameter p : safeParams(ep)) {
      if ("path".equals(p.in()) && !"integer".equals(p.schemaType())) {
        for (String payload : SecurityPayloads.PATH_TRAVERSAL) {
          cases.add(securityCase(ep, p, payload, "path traversal"));
        }
      }
    }

    if (ep.requestBody() != null) {
      for (String payload : SecurityPayloads.NOSQL_INJECTION) {
        cases.add(
            new TestCase(
                UUID.randomUUID().toString(),
                TestCategory.SECURITY,
                ep.path(),
                ep.method(),
                Map.of(),
                Map.of(),
                buildBasePathParams(ep),
                Map.of("field", payload),
                List.of(Assertion.statusCodeRange("4xx")),
                "Security: NoSQL injection in body — payload=" + payload,
                false));
      }
    }

    return cases;
  }

  private TestCase securityCase(Endpoint ep, Parameter p, String payload, String type) {
    Map<String, String> pathParams = buildBasePathParams(ep);
    Map<String, String> queryParams = new HashMap<>();
    if ("path".equals(p.in())) {
      pathParams.put(p.name(), payload);
    } else {
      queryParams.put(p.name(), payload);
    }
    return new TestCase(
        UUID.randomUUID().toString(),
        TestCategory.SECURITY,
        ep.path(),
        ep.method(),
        Map.of(),
        queryParams,
        pathParams,
        null,
        List.of(Assertion.statusCodeRange("4xx")),
        "Security: " + type + " in param=" + p.name(),
        false);
  }

  private Map<String, String> buildBasePathParams(Endpoint ep) {
    Map<String, String> params = new HashMap<>();
    for (Parameter p : safeParams(ep)) {
      if ("path".equals(p.in())) params.put(p.name(), "valid-value");
    }
    return params;
  }

  private List<Parameter> safeParams(Endpoint ep) {
    return ep.parameters() != null ? ep.parameters() : List.of();
  }
}
