package io.github.shazaanashraff.apiforge.modules.testgenerator;

import io.github.shazaanashraff.apiforge.modules.schemaparser.Endpoint;
import io.github.shazaanashraff.apiforge.modules.schemaparser.IdFormatHint;
import io.github.shazaanashraff.apiforge.modules.schemaparser.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class HappyPathGenerator implements TestCaseGenerator {

  @Override
  public TestCategory category() {
    return TestCategory.HAPPY_PATH;
  }

  @Override
  public List<TestCase> generate(TestGenerationContext ctx) {
    Endpoint ep = ctx.endpoint();
    Map<String, String> headers = buildAuthHeaders(ep);
    Map<String, String> pathParams = buildPathParams(ep, ctx);
    Map<String, String> queryParams = buildQueryParams(ep, ctx);
    Object body = buildBody(ep, ctx);

    List<Assertion> assertions = new ArrayList<>();
    assertions.add(Assertion.statusCodeRange("2xx"));
    assertions.add(Assertion.responseSchema());

    return List.of(
        new TestCase(
            UUID.randomUUID().toString(),
            TestCategory.HAPPY_PATH,
            ep.path(),
            ep.method(),
            headers,
            queryParams,
            pathParams,
            body,
            assertions,
            "Happy path: valid inputs for " + ep.method() + " " + ep.path(),
            false));
  }

  private Map<String, String> buildAuthHeaders(Endpoint ep) {
    Map<String, String> headers = new HashMap<>();
    if (ep.authRequirement() == null) return headers;
    switch (ep.authRequirement()) {
      case BEARER_JWT -> headers.put("Authorization", "Bearer <valid_token>");
      case API_KEY -> headers.put("X-API-Key", "<valid_api_key>");
      case BASIC -> headers.put("Authorization", "Basic <valid_credentials>");
      default -> {}
    }
    return headers;
  }

  private Map<String, String> buildPathParams(Endpoint ep, TestGenerationContext ctx) {
    Map<String, String> params = new HashMap<>();
    if (ep.parameters() == null) return params;
    for (Parameter p : ep.parameters()) {
      if ("path".equals(p.in())) {
        params.put(p.name(), generateParamValue(p, ep, ctx));
      }
    }
    return params;
  }

  private Map<String, String> buildQueryParams(Endpoint ep, TestGenerationContext ctx) {
    Map<String, String> params = new HashMap<>();
    if (ep.parameters() == null) return params;
    for (Parameter p : ep.parameters()) {
      if ("query".equals(p.in()) && p.required()) {
        params.put(p.name(), generateParamValue(p, ep, ctx));
      }
    }
    return params;
  }

  private Object buildBody(Endpoint ep, TestGenerationContext ctx) {
    if (ep.requestBody() == null || !ep.requestBody().required()) return null;
    return ctx.dataGenerator().generatePayload(ep.requestBody());
  }

  private String generateParamValue(Parameter p, Endpoint ep, TestGenerationContext ctx) {
    if (isIdParam(p)) {
      boolean useObjectId =
          ctx.mongoBackedApi()
              || (ep.idFormatHint() != null
                  && ep.idFormatHint().format() == IdFormatHint.Format.OBJECTID);
      return useObjectId
          ? ctx.dataGenerator().objectIds().validObjectId()
          : UUID.randomUUID().toString();
    }
    String type = p.schemaType() != null ? p.schemaType() : "string";
    return switch (type) {
      case "integer", "number" -> "1";
      case "boolean" -> "true";
      default -> "test-value";
    };
  }

  private boolean isIdParam(Parameter p) {
    String name = p.name().toLowerCase();
    return name.equals("id") || name.endsWith("id") || name.endsWith("_id");
  }
}
