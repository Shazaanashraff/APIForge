package io.github.shazaanashraff.apiforge.modules.testgenerator;

import io.github.shazaanashraff.apiforge.modules.schemaparser.Endpoint;
import io.github.shazaanashraff.apiforge.modules.schemaparser.IdFormatHint;
import io.github.shazaanashraff.apiforge.modules.schemaparser.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class MongoSpecificGenerator implements TestCaseGenerator {

  @Override
  public TestCategory category() {
    return TestCategory.MONGODB_SPECIFIC;
  }

  @Override
  public List<TestCase> generate(TestGenerationContext ctx) {
    Endpoint ep = ctx.endpoint();
    List<Parameter> objectIdParams = getObjectIdPathParams(ep, ctx);
    if (objectIdParams.isEmpty()) return List.of();

    List<TestCase> cases = new ArrayList<>();
    for (Parameter p : objectIdParams) {
      String validId = ctx.dataGenerator().objectIds().validObjectId();
      String tooShort = validId.substring(0, 23);
      String tooLong = validId + "f";

      cases.add(
          mongoCase(
              ep, p, validId, "valid ObjectId", false, List.of(Assertion.statusCodeRange("2xx"))));
      cases.add(
          mongoCase(
              ep,
              p,
              tooShort,
              "23-char hex (too short)",
              true,
              List.of(Assertion.statusCodeRange("4xx"))));
      cases.add(
          mongoCase(
              ep,
              p,
              tooLong,
              "25-char hex (too long)",
              true,
              List.of(Assertion.statusCodeRange("4xx"))));
      cases.add(
          mongoCase(
              ep,
              p,
              ctx.dataGenerator().objectIds().invalidObjectId(),
              "24 chars with non-hex characters",
              true,
              List.of(Assertion.statusCodeRange("4xx"))));
      cases.add(
          mongoCase(
              ep,
              p,
              ctx.dataGenerator().objectIds().uuidLookingObjectId(),
              "UUID instead of ObjectId",
              true,
              List.of(Assertion.statusCodeRange("4xx"))));
      cases.add(mongoCase(ep, p, "", "empty string", true, List.of(Assertion.statusCode(400))));
      cases.add(
          mongoCase(
              ep,
              p,
              "{$ne:null}",
              "NoSQL injection in path param",
              true,
              List.of(Assertion.statusCodeRange("4xx"))));
    }

    // NoSQL injection in query params
    if (ep.parameters() != null) {
      for (Parameter p : ep.parameters()) {
        if ("query".equals(p.in())) {
          cases.add(
              new TestCase(
                  UUID.randomUUID().toString(),
                  TestCategory.MONGODB_SPECIFIC,
                  ep.path(),
                  ep.method(),
                  Map.of(),
                  Map.of(p.name() + "[$ne]", "1"),
                  buildBasePathParams(ep, ctx),
                  null,
                  List.of(Assertion.statusCodeRange("4xx")),
                  "MongoDB: NoSQL injection in query param=" + p.name(),
                  true));
        }
      }
    }

    return cases;
  }

  private List<Parameter> getObjectIdPathParams(Endpoint ep, TestGenerationContext ctx) {
    if (ep.parameters() == null) return List.of();
    boolean looksMongoEndpoint =
        ctx.mongoBackedApi()
            || (ep.idFormatHint() != null
                && ep.idFormatHint().format() == IdFormatHint.Format.OBJECTID);
    if (!looksMongoEndpoint) return List.of();

    return ep.parameters().stream().filter(p -> "path".equals(p.in()) && isIdLike(p)).toList();
  }

  private boolean isIdLike(Parameter p) {
    String name = p.name().toLowerCase();
    return name.equals("id") || name.endsWith("id") || name.endsWith("_id");
  }

  private TestCase mongoCase(
      Endpoint ep,
      Parameter p,
      String idValue,
      String label,
      boolean applicableIfMongo,
      List<Assertion> assertions) {
    Map<String, String> pathParams = new HashMap<>();
    if (ep.parameters() != null) {
      for (Parameter param : ep.parameters()) {
        if ("path".equals(param.in())) {
          pathParams.put(param.name(), param.name().equals(p.name()) ? idValue : "valid-value");
        }
      }
    }
    return new TestCase(
        UUID.randomUUID().toString(),
        TestCategory.MONGODB_SPECIFIC,
        ep.path(),
        ep.method(),
        Map.of(),
        Map.of(),
        pathParams,
        null,
        assertions,
        "MongoDB: " + label + " for param=" + p.name(),
        applicableIfMongo);
  }

  private Map<String, String> buildBasePathParams(Endpoint ep, TestGenerationContext ctx) {
    Map<String, String> params = new HashMap<>();
    if (ep.parameters() == null) return params;
    for (Parameter p : ep.parameters()) {
      if ("path".equals(p.in())) {
        params.put(p.name(), ctx.dataGenerator().objectIds().validObjectId());
      }
    }
    return params;
  }
}
