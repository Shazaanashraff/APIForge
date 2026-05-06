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

class PayloadSizeGeneratorTest {

  private final PayloadSizeGenerator generator = new PayloadSizeGenerator();

  @Test
  void postWithDeclaredMaxGeneratesFourCases() {
    List<TestCase> cases = generator.generate(TestGenerationContext.of(postEndpointWithMax(1000)));
    assertThat(cases).hasSize(4);
  }

  @Test
  void firstCaseIsEmptyBody() {
    List<TestCase> cases = generator.generate(TestGenerationContext.of(postEndpointWithMax(1000)));
    assertThat(cases.get(0).description()).containsIgnoringCase("empty body");
  }

  @Test
  void secondCaseExpects2xx() {
    List<TestCase> cases = generator.generate(TestGenerationContext.of(postEndpointWithMax(1000)));
    TestCase justUnder = cases.get(1);
    assertThat(justUnder.expectedAssertions())
        .anyMatch(
            a ->
                a.type() == Assertion.AssertionType.STATUS_CODE_RANGE
                    && a.value().equals("2xx"));
  }

  @Test
  void thirdCaseExpects4xx() {
    List<TestCase> cases = generator.generate(TestGenerationContext.of(postEndpointWithMax(1000)));
    TestCase justOver = cases.get(2);
    assertThat(justOver.expectedAssertions())
        .anyMatch(
            a ->
                a.type() == Assertion.AssertionType.STATUS_CODE_RANGE
                    && a.value().equals("4xx"));
  }

  @Test
  void postWithoutDeclaredMaxGeneratesThreeCases() {
    Endpoint ep =
        new Endpoint(
            "/items",
            HttpMethod.POST,
            "createItem",
            "Create item",
            List.of(),
            new RequestBodySchema(true, "application/json", null),
            Map.of(201, new ResponseSchema(201, "Created", null)),
            AuthRequirement.NONE,
            PaginationHint.none(),
            PayloadSizeHint.unknown(),
            SlaHint.none(),
            IdFormatHint.unknown(),
            List.of());

    // unknown: empty + 1MB + 10MB = 3 cases
    List<TestCase> cases = generator.generate(TestGenerationContext.of(ep));
    assertThat(cases).hasSize(3);
  }

  @Test
  void getEndpointGeneratesNoCases() {
    Endpoint ep =
        new Endpoint(
            "/items/{id}",
            HttpMethod.GET,
            "getItem",
            "Get item",
            List.of(new Parameter("id", "path", true, "string", null, "ID")),
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

  private Endpoint postEndpointWithMax(long maxBytes) {
    return new Endpoint(
        "/items",
        HttpMethod.POST,
        "createItem",
        "Create item",
        List.of(),
        new RequestBodySchema(
            true,
            "application/json",
            "{\"type\":\"object\",\"properties\":{\"data\":{\"type\":\"string\"}}}"),
        Map.of(201, new ResponseSchema(201, "Created", null)),
        AuthRequirement.NONE,
        PaginationHint.none(),
        new PayloadSizeHint(maxBytes, true),
        SlaHint.none(),
        IdFormatHint.unknown(),
        List.of());
  }
}
