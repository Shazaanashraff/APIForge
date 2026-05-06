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

class PaginationGeneratorTest {

  private final PaginationGenerator generator = new PaginationGenerator();

  @Test
  void offsetLimitEndpointGeneratesAtLeastSixCases() {
    List<TestCase> cases = generator.generate(TestGenerationContext.of(offsetLimitEndpoint()));
    assertThat(cases).hasSizeGreaterThanOrEqualTo(6);
  }

  @Test
  void offsetLimitCasesIncludeNegativeOffset() {
    List<TestCase> cases = generator.generate(TestGenerationContext.of(offsetLimitEndpoint()));
    assertThat(cases).anyMatch(tc -> "-1".equals(tc.queryParams().get("offset")));
  }

  @Test
  void offsetLimitCasesIncludeLargeLimit() {
    List<TestCase> cases = generator.generate(TestGenerationContext.of(offsetLimitEndpoint()));
    assertThat(cases).anyMatch(tc -> "10000".equals(tc.queryParams().get("limit")));
  }

  @Test
  void pageSizeEndpointGeneratesAtLeastSixCases() {
    Endpoint ep =
        new Endpoint(
            "/pages",
            HttpMethod.GET,
            "listPages",
            "List pages",
            List.of(
                new Parameter("page", "query", false, "integer", null, "Page"),
                new Parameter("size", "query", false, "integer", null, "Size")),
            null,
            Map.of(200, new ResponseSchema(200, "OK", null)),
            AuthRequirement.NONE,
            new PaginationHint(PaginationHint.Style.PAGE_SIZE, List.of("page", "size")),
            PayloadSizeHint.unknown(),
            SlaHint.none(),
            IdFormatHint.unknown(),
            List.of());

    List<TestCase> cases = generator.generate(TestGenerationContext.of(ep));
    assertThat(cases).hasSizeGreaterThanOrEqualTo(6);
  }

  @Test
  void cursorEndpointGeneratesAtLeastThreeCases() {
    Endpoint ep =
        new Endpoint(
            "/feed",
            HttpMethod.GET,
            "getFeed",
            "Get feed",
            List.of(new Parameter("cursor", "query", false, "string", null, "Cursor")),
            null,
            Map.of(200, new ResponseSchema(200, "OK", null)),
            AuthRequirement.NONE,
            new PaginationHint(PaginationHint.Style.CURSOR, List.of("cursor")),
            PayloadSizeHint.unknown(),
            SlaHint.none(),
            IdFormatHint.unknown(),
            List.of());

    List<TestCase> cases = generator.generate(TestGenerationContext.of(ep));
    assertThat(cases).hasSizeGreaterThanOrEqualTo(3);
  }

  @Test
  void nonPaginatedEndpointGeneratesNoCases() {
    Endpoint ep =
        new Endpoint(
            "/item/{id}",
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

  @Test
  void allCasesHavePaginationCategory() {
    List<TestCase> cases = generator.generate(TestGenerationContext.of(offsetLimitEndpoint()));
    assertThat(cases).allMatch(tc -> tc.category() == TestCategory.PAGINATION);
  }

  private Endpoint offsetLimitEndpoint() {
    return new Endpoint(
        "/items",
        HttpMethod.GET,
        "listItems",
        "List items",
        List.of(
            new Parameter("offset", "query", false, "integer", null, "Offset"),
            new Parameter("limit", "query", false, "integer", null, "Limit")),
        null,
        Map.of(200, new ResponseSchema(200, "OK", null)),
        AuthRequirement.NONE,
        new PaginationHint(PaginationHint.Style.OFFSET_LIMIT, List.of("offset", "limit")),
        PayloadSizeHint.unknown(),
        SlaHint.none(),
        IdFormatHint.unknown(),
        List.of());
  }
}
