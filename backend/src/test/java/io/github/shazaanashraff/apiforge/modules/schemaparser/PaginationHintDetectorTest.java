package io.github.shazaanashraff.apiforge.modules.schemaparser;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaginationHintDetectorTest {

  private PaginationHintDetector detector;

  @BeforeEach
  void setUp() {
    detector = new PaginationHintDetector();
  }

  private static Parameter queryParam(String name) {
    return new Parameter(name, "query", false, "integer", null, null);
  }

  @Test
  void detectsOffsetLimit() {
    List<Parameter> params = List.of(queryParam("offset"), queryParam("limit"));

    PaginationHint hint = detector.detect(params);

    assertThat(hint.style()).isEqualTo(PaginationHint.Style.OFFSET_LIMIT);
    assertThat(hint.paramNames()).containsExactlyInAnyOrder("offset", "limit");
  }

  @Test
  void detectsPageSize() {
    List<Parameter> params = List.of(queryParam("page"), queryParam("size"));

    PaginationHint hint = detector.detect(params);

    assertThat(hint.style()).isEqualTo(PaginationHint.Style.PAGE_SIZE);
    assertThat(hint.paramNames()).containsExactlyInAnyOrder("page", "size");
  }

  @Test
  void detectsPagePerPage() {
    List<Parameter> params = List.of(queryParam("page"), queryParam("per_page"));

    PaginationHint hint = detector.detect(params);

    assertThat(hint.style()).isEqualTo(PaginationHint.Style.PAGE_SIZE);
  }

  @Test
  void detectsCursorAfterBefore() {
    List<Parameter> params = List.of(queryParam("after"), queryParam("before"));

    PaginationHint hint = detector.detect(params);

    assertThat(hint.style()).isEqualTo(PaginationHint.Style.CURSOR);
  }

  @Test
  void detectsCursorParam() {
    List<Parameter> params = List.of(queryParam("cursor"), queryParam("limit"));

    PaginationHint hint = detector.detect(params);

    // CURSOR takes precedence over OFFSET_LIMIT
    assertThat(hint.style()).isEqualTo(PaginationHint.Style.CURSOR);
  }

  @Test
  void returnsNoneForNonPaginationParams() {
    List<Parameter> params = List.of(queryParam("status"), queryParam("category"));

    PaginationHint hint = detector.detect(params);

    assertThat(hint.style()).isEqualTo(PaginationHint.Style.NONE);
    assertThat(hint.paramNames()).isEmpty();
  }

  @Test
  void returnsNoneForPathParamsOnly() {
    // Path params are not "query" in — should not be detected as pagination
    Parameter pathId = new Parameter("id", "path", true, "string", null, null);

    PaginationHint hint = detector.detect(List.of(pathId));

    assertThat(hint.style()).isEqualTo(PaginationHint.Style.NONE);
  }

  @Test
  void returnsNoneForEmptyParamList() {
    assertThat(detector.detect(List.of()).style()).isEqualTo(PaginationHint.Style.NONE);
  }
}
