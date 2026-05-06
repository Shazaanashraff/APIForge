package io.github.shazaanashraff.apiforge.modules.testgenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.shazaanashraff.apiforge.modules.schemaparser.Endpoint;
import io.github.shazaanashraff.apiforge.modules.schemaparser.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class BoundaryGenerator implements TestCaseGenerator {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Override
  public TestCategory category() {
    return TestCategory.BOUNDARY;
  }

  @Override
  public List<TestCase> generate(TestGenerationContext ctx) {
    Endpoint ep = ctx.endpoint();
    List<TestCase> cases = new ArrayList<>();

    if (ep.parameters() == null) return cases;
    for (Parameter p : ep.parameters()) {
      if (!"path".equals(p.in()) && !"query".equals(p.in())) continue;
      ObjectNode schema = typeSchema(p.schemaType(), p.schemaFormat());
      List<Object> boundaries = ctx.dataGenerator().generateBoundaryValues(schema);
      for (Object boundary : boundaries) {
        cases.add(boundaryCase(ep, p, boundary, ctx));
      }
    }
    return cases;
  }

  private TestCase boundaryCase(Endpoint ep, Parameter p, Object value, TestGenerationContext ctx) {
    Map<String, String> pathParams = buildBasePathParams(ep);
    Map<String, String> queryParams = new HashMap<>();
    String strValue = value == null ? "" : String.valueOf(value);

    if ("path".equals(p.in())) {
      pathParams.put(p.name(), strValue);
    } else {
      queryParams.put(p.name(), strValue);
    }

    return new TestCase(
        UUID.randomUUID().toString(),
        TestCategory.BOUNDARY,
        ep.path(),
        ep.method(),
        Map.of(),
        queryParams,
        pathParams,
        null,
        List.of(Assertion.statusCodeRange("any")),
        "Boundary: param=" + p.name() + " value=" + strValue,
        false);
  }

  private Map<String, String> buildBasePathParams(Endpoint ep) {
    Map<String, String> params = new HashMap<>();
    if (ep.parameters() == null) return params;
    for (Parameter p : ep.parameters()) {
      if ("path".equals(p.in())) params.put(p.name(), "baseline-value");
    }
    return params;
  }

  private ObjectNode typeSchema(String type, String format) {
    ObjectNode node = MAPPER.createObjectNode();
    node.put("type", type != null ? type : "string");
    if (format != null) node.put("format", format);
    return node;
  }
}
