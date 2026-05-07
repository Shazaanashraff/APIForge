package io.github.shazaanashraff.apiforge.modules.schemaparser;

import java.util.List;

/**
 * Describes the pagination pattern detected from an endpoint's query parameters.
 *
 * @param style the pagination style; {@code NONE} if no pagination was detected
 * @param paramNames the query param names that triggered the detection (for test generation)
 */
public record PaginationHint(Style style, List<String> paramNames) {

  public enum Style {
    /** e.g. {@code ?offset=0&limit=20} */
    OFFSET_LIMIT,
    /** e.g. {@code ?page=0&size=20} or {@code ?page=1&per_page=25} */
    PAGE_SIZE,
    /** e.g. {@code ?cursor=abc} or {@code ?after=xyz&before=abc} */
    CURSOR,
    NONE
  }

  public static PaginationHint none() {
    return new PaginationHint(Style.NONE, List.of());
  }
}
