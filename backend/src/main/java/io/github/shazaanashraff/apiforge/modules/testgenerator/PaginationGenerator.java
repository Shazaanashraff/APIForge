package io.github.shazaanashraff.apiforge.modules.testgenerator;

import io.github.shazaanashraff.apiforge.modules.schemaparser.Endpoint;
import io.github.shazaanashraff.apiforge.modules.schemaparser.PaginationHint;
import io.github.shazaanashraff.apiforge.modules.schemaparser.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class PaginationGenerator implements TestCaseGenerator {

  @Override
  public TestCategory category() {
    return TestCategory.PAGINATION;
  }

  @Override
  public List<TestCase> generate(TestGenerationContext ctx) {
    Endpoint ep = ctx.endpoint();
    PaginationHint hint = ep.paginationHint();
    if (hint == null || hint.style() == PaginationHint.Style.NONE) return List.of();

    return switch (hint.style()) {
      case OFFSET_LIMIT -> generateOffsetLimit(ep, hint);
      case PAGE_SIZE -> generatePageSize(ep, hint);
      case CURSOR -> generateCursor(ep, hint);
      default -> List.of();
    };
  }

  private List<TestCase> generateOffsetLimit(Endpoint ep, PaginationHint hint) {
    String offset = paramName(hint, "offset");
    String limit = paramName(hint, "limit");
    List<TestCase> cases = new ArrayList<>();
    cases.add(
        paginationCase(
            ep,
            Map.of(offset, "0", limit, "10"),
            "first page",
            List.of(Assertion.statusCodeRange("2xx"))));
    cases.add(
        paginationCase(
            ep,
            Map.of(offset, "10", limit, "10"),
            "second page",
            List.of(Assertion.statusCodeRange("2xx"))));
    cases.add(
        paginationCase(
            ep,
            Map.of(offset, "999999", limit, "10"),
            "empty page (offset=999999)",
            List.of(Assertion.statusCodeRange("2xx"))));
    cases.add(
        paginationCase(
            ep,
            Map.of(offset, "-1", limit, "10"),
            "negative offset",
            List.of(Assertion.statusCodeRange("any"))));
    cases.add(
        paginationCase(
            ep,
            Map.of(offset, "0", limit, "0"),
            "limit=0",
            List.of(Assertion.statusCodeRange("any"))));
    cases.add(
        paginationCase(
            ep,
            Map.of(offset, "0", limit, "10000"),
            "limit=10000 (large)",
            List.of(Assertion.statusCodeRange("any"))));
    return cases;
  }

  private List<TestCase> generatePageSize(Endpoint ep, PaginationHint hint) {
    String page = paramName(hint, "page");
    String size = paramName(hint, "size", "limit", "per_page");
    List<TestCase> cases = new ArrayList<>();
    cases.add(
        paginationCase(
            ep,
            Map.of(page, "0", size, "10"),
            "first page",
            List.of(Assertion.statusCodeRange("2xx"))));
    cases.add(
        paginationCase(
            ep,
            Map.of(page, "1", size, "10"),
            "second page",
            List.of(Assertion.statusCodeRange("2xx"))));
    cases.add(
        paginationCase(
            ep,
            Map.of(page, "999999", size, "10"),
            "page beyond data",
            List.of(Assertion.statusCodeRange("2xx"))));
    cases.add(
        paginationCase(
            ep,
            Map.of(page, "-1", size, "10"),
            "negative page",
            List.of(Assertion.statusCodeRange("any"))));
    cases.add(
        paginationCase(
            ep, Map.of(page, "0", size, "0"), "size=0", List.of(Assertion.statusCodeRange("any"))));
    cases.add(
        paginationCase(
            ep,
            Map.of(page, "0", size, "10000"),
            "size=10000 (large)",
            List.of(Assertion.statusCodeRange("any"))));
    return cases;
  }

  private List<TestCase> generateCursor(Endpoint ep, PaginationHint hint) {
    String cursor = paramName(hint, "cursor", "after");
    List<TestCase> cases = new ArrayList<>();
    cases.add(
        paginationCase(
            ep, Map.of(), "first page (no cursor)", List.of(Assertion.statusCodeRange("2xx"))));
    cases.add(
        paginationCase(
            ep,
            Map.of(cursor, "invalid-cursor-xyz"),
            "invalid cursor string",
            List.of(Assertion.statusCodeRange("any"))));
    cases.add(
        paginationCase(
            ep,
            Map.of(cursor, "x".repeat(1024)),
            "very long cursor",
            List.of(Assertion.statusCodeRange("any"))));
    return cases;
  }

  private TestCase paginationCase(
      Endpoint ep, Map<String, String> queryParams, String label, List<Assertion> assertions) {
    return new TestCase(
        UUID.randomUUID().toString(),
        TestCategory.PAGINATION,
        ep.path(),
        ep.method(),
        Map.of(),
        queryParams,
        buildPathParams(ep),
        null,
        assertions,
        "Pagination: " + label + " — " + ep.path(),
        false);
  }

  private Map<String, String> buildPathParams(Endpoint ep) {
    Map<String, String> params = new HashMap<>();
    if (ep.parameters() == null) return params;
    for (Parameter p : ep.parameters()) {
      if ("path".equals(p.in())) params.put(p.name(), "test-value");
    }
    return params;
  }

  private String paramName(PaginationHint hint, String... candidates) {
    if (hint.paramNames() != null) {
      for (String c : candidates) {
        if (hint.paramNames().contains(c)) return c;
      }
    }
    return candidates[0];
  }
}
