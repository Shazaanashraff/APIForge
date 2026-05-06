package io.github.shazaanashraff.apiforge.modules.testgenerator;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.shazaanashraff.apiforge.modules.schemaparser.AuthRequirement;
import io.github.shazaanashraff.apiforge.modules.schemaparser.Endpoint;
import io.github.shazaanashraff.apiforge.modules.schemaparser.IdFormatHint;
import io.github.shazaanashraff.apiforge.modules.schemaparser.PaginationHint;
import io.github.shazaanashraff.apiforge.modules.schemaparser.Parameter;
import io.github.shazaanashraff.apiforge.modules.schemaparser.PayloadSizeHint;
import io.github.shazaanashraff.apiforge.modules.schemaparser.ResponseSchema;
import io.github.shazaanashraff.apiforge.modules.schemaparser.SlaHint;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

class MongoSpecificGeneratorTest {

  private final MongoSpecificGenerator generator = new MongoSpecificGenerator();

  @Test
  void objectIdPathParamGeneratesAtLeastSevenCases() {
    List<TestCase> cases = generator.generate(TestGenerationContext.of(objectIdEndpoint()));
    assertThat(cases).hasSizeGreaterThanOrEqualTo(7);
  }

  @Test
  void allCasesHaveMongodbSpecificCategory() {
    List<TestCase> cases = generator.generate(TestGenerationContext.of(objectIdEndpoint()));
    assertThat(cases).allMatch(tc -> tc.category() == TestCategory.MONGODB_SPECIFIC);
  }

  @Test
  void validObjectIdCaseExpects2xx() {
    List<TestCase> cases = generator.generate(TestGenerationContext.of(objectIdEndpoint()));

    assertThat(cases)
        .anyMatch(
            tc ->
                tc.description().contains("valid ObjectId")
                    && tc.expectedAssertions().stream()
                        .anyMatch(
                            a ->
                                a.type() == Assertion.AssertionType.STATUS_CODE_RANGE
                                    && "2xx".equals(a.value())));
  }

  @Test
  void nosqlInjectionCaseIsPresent() {
    List<TestCase> cases = generator.generate(TestGenerationContext.of(objectIdEndpoint()));

    assertThat(cases)
        .anyMatch(
            tc ->
                tc.description().toLowerCase().contains("nosql injection")
                    || tc.pathParams().values().stream().anyMatch(v -> v.contains("$")));
  }

  @Test
  void tooShortAndTooLongCasesArePresent() {
    List<TestCase> cases = generator.generate(TestGenerationContext.of(objectIdEndpoint()));

    assertThat(cases).anyMatch(tc -> tc.description().contains("too short"));
    assertThat(cases).anyMatch(tc -> tc.description().contains("too long"));
  }

  @Test
  void nonMongoEndpointGeneratesNoCases() {
    Endpoint ep =
        new Endpoint(
            "/items/{id}",
            HttpMethod.GET,
            "getItem",
            "Get item",
            List.of(new Parameter("id", "path", true, "string", "uuid", "UUID")),
            null,
            Map.of(200, new ResponseSchema(200, "OK", null)),
            AuthRequirement.NONE,
            PaginationHint.none(),
            PayloadSizeHint.unknown(),
            SlaHint.none(),
            IdFormatHint.unknown(),
            List.of());

    assertThat(generator.generate(TestGenerationContext.of(ep))).isEmpty();
  }

  @Test
  void mongoBackedContextGeneratesCasesEvenWithUnknownFormat() {
    Endpoint ep =
        new Endpoint(
            "/docs/{id}",
            HttpMethod.GET,
            "getDoc",
            "Get doc",
            List.of(new Parameter("id", "path", true, "string", null, "ID")),
            null,
            Map.of(200, new ResponseSchema(200, "OK", null)),
            AuthRequirement.NONE,
            PaginationHint.none(),
            PayloadSizeHint.unknown(),
            SlaHint.none(),
            IdFormatHint.unknown(),
            List.of());

    List<TestCase> cases = generator.generate(TestGenerationContext.forMongo(ep));
    assertThat(cases).hasSizeGreaterThanOrEqualTo(7);
  }

  private Endpoint objectIdEndpoint() {
    return new Endpoint(
        "/documents/{id}",
        HttpMethod.GET,
        "getDocument",
        "Get document",
        List.of(new Parameter("id", "path", true, "string", null, "Document ObjectId")),
        null,
        Map.of(200, new ResponseSchema(200, "OK", null)),
        AuthRequirement.NONE,
        PaginationHint.none(),
        PayloadSizeHint.unknown(),
        SlaHint.none(),
        new IdFormatHint(IdFormatHint.Format.OBJECTID),
        List.of());
  }
}
