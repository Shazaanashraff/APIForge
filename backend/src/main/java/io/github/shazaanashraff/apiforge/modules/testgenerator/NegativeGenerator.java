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

class NegativeGenerator implements TestCaseGenerator {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Override
  public TestCategory category() {
    return TestCategory.NEGATIVE;
  }

  @Override
  public List<TestCase> generate(TestGenerationContext ctx) {
    Endpoint ep = ctx.endpoint();
    List<TestCase> cases = new ArrayList<>();

    if (ep.parameters() != null) {
      for (Parameter p : ep.parameters()) {
        if (!p.required()) continue;
        ObjectNode schema = buildSchema(p.schemaType());
        Object invalid = ctx.dataGenerator().generateInvalidValue(schema);
        cases.add(
            paramCase(ep, p, String.valueOf(invalid), "wrong type", List.of(Assertion.statusCode(400))));
        cases.add(
            paramCase(ep, p, "", "empty/null value", List.of(Assertion.statusCode(400))));
      }
    }

    if (ep.requestBody() != null && ep.requestBody().required()) {
      cases.add(
          new TestCase(
              UUID.randomUUID().toString(),
              TestCategory.NEGATIVE,
              ep.path(),
              ep.method(),
              Map.of(),
              Map.of(),
              Map.of(),
              Map.of(),
              List.of(Assertion.statusCode(400)),
              "Negative: empty body when body is required",
              false));
    }

    return cases;
  }

  private TestCase paramCase(
      Endpoint ep, Parameter p, String value, String reason, List<Assertion> assertions) {
    Map<String, String> pathParams = new HashMap<>();
    Map<String, String> queryParams = new HashMap<>();
    if (ep.parameters() != null) {
      for (Parameter base : ep.parameters()) {
        if ("path".equals(base.in())) {
          pathParams.put(base.name(), base.name().equals(p.name()) ? value : "valid-value");
        } else if ("query".equals(base.in()) && base.required()) {
          queryParams.put(base.name(), base.name().equals(p.name()) ? value : "valid-value");
        }
      }
    }
    return new TestCase(
        UUID.randomUUID().toString(),
        TestCategory.NEGATIVE,
        ep.path(),
        ep.method(),
        Map.of(),
        queryParams,
        pathParams,
        null,
        assertions,
        "Negative: param=" + p.name() + " — " + reason,
        false);
  }

  private ObjectNode buildSchema(String type) {
    ObjectNode n = MAPPER.createObjectNode();
    n.put("type", type != null ? type : "string");
    return n;
  }
}
