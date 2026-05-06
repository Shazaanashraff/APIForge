package io.github.shazaanashraff.apiforge.modules.testgenerator;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.shazaanashraff.apiforge.modules.schemaparser.AuthRequirement;
import io.github.shazaanashraff.apiforge.modules.schemaparser.Endpoint;
import io.github.shazaanashraff.apiforge.modules.schemaparser.IdFormatHint;
import io.github.shazaanashraff.apiforge.modules.schemaparser.PaginationHint;
import io.github.shazaanashraff.apiforge.modules.schemaparser.Parameter;
import io.github.shazaanashraff.apiforge.modules.schemaparser.PayloadSizeHint;
import io.github.shazaanashraff.apiforge.modules.schemaparser.RequestBodySchema;
import io.github.shazaanashraff.apiforge.modules.schemaparser.ResponseSchema;
import io.github.shazaanashraff.apiforge.modules.schemaparser.SlaHint;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

class HappyPathGeneratorTest {

  private final HappyPathGenerator generator = new HappyPathGenerator();

  @Test
  void getPetsByIdGeneratesOneTestCase() {
    Endpoint ep =
        new Endpoint(
            "/pets/{id}",
            HttpMethod.GET,
            "getPetById",
            "Get pet by ID",
            List.of(new Parameter("id", "path", true, "string", "uuid", "Pet ID")),
            null,
            Map.of(200, new ResponseSchema(200, "OK", null)),
            AuthRequirement.NONE,
            PaginationHint.none(),
            PayloadSizeHint.unknown(),
            SlaHint.none(),
            IdFormatHint.unknown(),
            List.of("pets"));

    List<TestCase> cases = generator.generate(TestGenerationContext.of(ep));

    assertThat(cases).hasSize(1);
    TestCase tc = cases.get(0);
    assertThat(tc.category()).isEqualTo(TestCategory.HAPPY_PATH);
    assertThat(tc.method()).isEqualTo(HttpMethod.GET);
    assertThat(tc.endpointPath()).isEqualTo("/pets/{id}");
    assertThat(tc.pathParams()).containsKey("id");
    assertThat(tc.expectedAssertions()).isNotEmpty();
  }

  @Test
  void happyPathContainsStatusCodeRangeAssertion() {
    Endpoint ep =
        new Endpoint(
            "/items",
            HttpMethod.GET,
            "listItems",
            "List items",
            List.of(),
            null,
            Map.of(200, new ResponseSchema(200, "OK", null)),
            AuthRequirement.NONE,
            PaginationHint.none(),
            PayloadSizeHint.unknown(),
            SlaHint.none(),
            IdFormatHint.unknown(),
            List.of());

    List<TestCase> cases = generator.generate(TestGenerationContext.of(ep));

    assertThat(cases.get(0).expectedAssertions())
        .anyMatch(a -> a.type() == Assertion.AssertionType.STATUS_CODE_RANGE);
  }

  @Test
  void happyPathForSecuredEndpointAddsAuthHeader() {
    Endpoint ep =
        new Endpoint(
            "/secured",
            HttpMethod.GET,
            "securedOp",
            "Secured",
            List.of(),
            null,
            Map.of(200, new ResponseSchema(200, "OK", null)),
            AuthRequirement.BEARER_JWT,
            PaginationHint.none(),
            PayloadSizeHint.unknown(),
            SlaHint.none(),
            IdFormatHint.unknown(),
            List.of());

    List<TestCase> cases = generator.generate(TestGenerationContext.of(ep));

    assertThat(cases.get(0).headers()).containsKey("Authorization");
  }

  @Test
  void happyPathForPostWithBodyIncludesRequestBody() {
    Endpoint ep =
        new Endpoint(
            "/pets",
            HttpMethod.POST,
            "createPet",
            "Create pet",
            List.of(),
            new RequestBodySchema(
                true,
                "application/json",
                "{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"}},\"required\":[\"name\"]}"),
            Map.of(201, new ResponseSchema(201, "Created", null)),
            AuthRequirement.NONE,
            PaginationHint.none(),
            PayloadSizeHint.unknown(),
            SlaHint.none(),
            IdFormatHint.unknown(),
            List.of());

    List<TestCase> cases = generator.generate(TestGenerationContext.of(ep));

    assertThat(cases).hasSize(1);
    assertThat(cases.get(0).requestBody()).isNotNull();
  }
}
