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

class SecurityGeneratorTest {

  private final SecurityGenerator generator = new SecurityGenerator();

  @Test
  void postWithBodyContainsNoSqlInjectionCases() {
    Endpoint ep =
        new Endpoint(
            "/users",
            HttpMethod.POST,
            "createUser",
            "Create user",
            List.of(),
            new RequestBodySchema(
                true,
                "application/json",
                "{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"}}}"),
            Map.of(201, new ResponseSchema(201, "Created", null)),
            AuthRequirement.NONE,
            PaginationHint.none(),
            PayloadSizeHint.unknown(),
            SlaHint.none(),
            IdFormatHint.unknown(),
            List.of());

    List<TestCase> cases = generator.generate(TestGenerationContext.of(ep));

    assertThat(cases).isNotEmpty();
    assertThat(cases)
        .anyMatch(
            tc ->
                tc.description().toLowerCase().contains("nosql")
                    || tc.description().toLowerCase().contains("injection"));
  }

  @Test
  void endpointWithQueryParamContainsSqlInjectionCases() {
    List<TestCase> cases = generator.generate(TestGenerationContext.of(searchEndpoint()));

    assertThat(cases).anyMatch(tc -> tc.description().toLowerCase().contains("sql injection"));
  }

  @Test
  void endpointWithQueryParamContainsXssCases() {
    List<TestCase> cases = generator.generate(TestGenerationContext.of(searchEndpoint()));

    assertThat(cases).anyMatch(tc -> tc.description().toLowerCase().contains("xss"));
  }

  @Test
  void allCasesHaveSecurityCategory() {
    List<TestCase> cases = generator.generate(TestGenerationContext.of(searchEndpoint()));

    assertThat(cases).isNotEmpty();
    assertThat(cases).allMatch(tc -> tc.category() == TestCategory.SECURITY);
  }

  @Test
  void endpointWithPathParamContainsPathTraversalCases() {
    Endpoint ep =
        new Endpoint(
            "/files/{filename}",
            HttpMethod.GET,
            "getFile",
            "Get file",
            List.of(new Parameter("filename", "path", true, "string", null, "Filename")),
            null,
            Map.of(200, new ResponseSchema(200, "OK", null)),
            AuthRequirement.NONE,
            PaginationHint.none(),
            PayloadSizeHint.unknown(),
            SlaHint.none(),
            IdFormatHint.unknown(),
            List.of());

    List<TestCase> cases = generator.generate(TestGenerationContext.of(ep));

    assertThat(cases).anyMatch(tc -> tc.description().toLowerCase().contains("path traversal"));
  }

  private Endpoint searchEndpoint() {
    return new Endpoint(
        "/search",
        HttpMethod.GET,
        "search",
        "Search",
        List.of(new Parameter("q", "query", true, "string", null, "Query")),
        null,
        Map.of(200, new ResponseSchema(200, "OK", null)),
        AuthRequirement.NONE,
        PaginationHint.none(),
        PayloadSizeHint.unknown(),
        SlaHint.none(),
        IdFormatHint.unknown(),
        List.of());
  }
}
