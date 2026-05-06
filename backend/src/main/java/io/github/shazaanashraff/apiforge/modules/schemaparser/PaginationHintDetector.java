package io.github.shazaanashraff.apiforge.modules.schemaparser;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Infers the pagination style of an endpoint from its query parameter names.
 *
 * <p>Detection priority (first match wins):
 * <ol>
 *   <li>CURSOR — presence of "cursor", "after", "before", "next_token", "page_token"
 *   <li>PAGE_SIZE — presence of "page" paired with "size", "limit", or "per_page"
 *   <li>OFFSET_LIMIT — presence of both "offset" and "limit"
 *   <li>NONE — no known pagination params
 * </ol>
 */
@Component
public class PaginationHintDetector {

  private static final Set<String> CURSOR_PARAMS =
      Set.of("cursor", "after", "before", "next_token", "page_token");
  private static final Set<String> PAGE_PARAMS = Set.of("page", "page_number", "page_num");
  private static final Set<String> SIZE_PARAMS = Set.of("size", "limit", "per_page", "page_size");
  private static final Set<String> OFFSET_PARAMS = Set.of("offset", "from", "start");

  public PaginationHint detect(List<Parameter> queryParams) {
    List<String> names =
        queryParams.stream()
            .filter(p -> "query".equals(p.in()))
            .map(p -> p.name().toLowerCase())
            .collect(Collectors.toList());

    Set<String> nameSet = Set.copyOf(names);

    // 1. Cursor-based
    List<String> cursorMatches = names.stream().filter(CURSOR_PARAMS::contains).toList();
    if (!cursorMatches.isEmpty()) {
      return new PaginationHint(PaginationHint.Style.CURSOR, cursorMatches);
    }

    // 2. Page + size
    List<String> pageMatches = names.stream().filter(PAGE_PARAMS::contains).toList();
    List<String> sizeMatches = names.stream().filter(SIZE_PARAMS::contains).toList();
    if (!pageMatches.isEmpty() && !sizeMatches.isEmpty()) {
      return new PaginationHint(
          PaginationHint.Style.PAGE_SIZE,
          List.of(pageMatches.get(0), sizeMatches.get(0)));
    }

    // 3. Offset + limit
    List<String> offsetMatches = names.stream().filter(OFFSET_PARAMS::contains).toList();
    List<String> limitForOffset = names.stream().filter(n -> "limit".equals(n)).toList();
    if (!offsetMatches.isEmpty() && !limitForOffset.isEmpty()) {
      return new PaginationHint(
          PaginationHint.Style.OFFSET_LIMIT,
          List.of(offsetMatches.get(0), limitForOffset.get(0)));
    }

    // 4. Size/limit alone (single-param pagination — less common but valid)
    if (!sizeMatches.isEmpty() && pageMatches.isEmpty()) {
      // Only a limit without offset/page — treat as NONE (ambiguous)
    }

    return PaginationHint.none();
  }
}
