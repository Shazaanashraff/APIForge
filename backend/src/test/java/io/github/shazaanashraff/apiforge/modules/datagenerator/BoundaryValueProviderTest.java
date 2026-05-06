package io.github.shazaanashraff.apiforge.modules.datagenerator;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BoundaryValueProviderTest {

  private BoundaryValueProvider provider;
  private ObjectMapper om;

  @BeforeEach
  void setUp() {
    provider = new BoundaryValueProvider();
    om = new ObjectMapper();
  }

  private ObjectNode intSchema(long min, long max) {
    ObjectNode n = om.createObjectNode();
    n.put("type", "integer");
    n.put("minimum", min);
    n.put("maximum", max);
    return n;
  }

  private ObjectNode strSchema(int minLen, int maxLen) {
    ObjectNode n = om.createObjectNode();
    n.put("type", "string");
    n.put("minLength", minLen);
    n.put("maxLength", maxLen);
    return n;
  }

  private ObjectNode arraySchema(int minItems, int maxItems) {
    ObjectNode n = om.createObjectNode();
    n.put("type", "array");
    n.put("minItems", minItems);
    n.put("maxItems", maxItems);
    return n;
  }

  // ── Integer boundaries ─────────────────────────────────────────────────────

  @Test
  void integerBoundariesIncludeMinAndMax() {
    List<Long> values = provider.integerBoundaries(intSchema(5, 10));
    assertThat(values).contains(5L, 10L);
  }

  @Test
  void integerBoundariesIncludeBelowMinAndAboveMax() {
    List<Long> values = provider.integerBoundaries(intSchema(5, 10));
    assertThat(values).contains(4L, 11L); // min-1 and max+1
  }

  @Test
  void integerBoundariesIncludeJustAboveMin() {
    List<Long> values = provider.integerBoundaries(intSchema(5, 10));
    assertThat(values).contains(6L); // min+1
  }

  @Test
  void integerBoundariesHaveAtLeast5Values() {
    List<Long> values = provider.integerBoundaries(intSchema(0, 100));
    assertThat(values).hasSizeGreaterThanOrEqualTo(5);
  }

  // ── String boundaries ─────────────────────────────────────────────────────

  @Test
  void stringBoundariesIncludeEmptyString() {
    List<String> values = provider.stringBoundaries(strSchema(2, 10));
    assertThat(values).contains("");
  }

  @Test
  void stringBoundariesIncludeAtMinLength() {
    List<String> values = provider.stringBoundaries(strSchema(3, 10));
    assertThat(values).anyMatch(s -> s.length() == 3);
  }

  @Test
  void stringBoundariesIncludeAtMaxLength() {
    List<String> values = provider.stringBoundaries(strSchema(3, 10));
    assertThat(values).anyMatch(s -> s.length() == 10);
  }

  @Test
  void stringBoundariesIncludeOverMaxLength() {
    List<String> values = provider.stringBoundaries(strSchema(3, 10));
    assertThat(values).anyMatch(s -> s.length() == 11); // maxLength + 1
  }

  @Test
  void stringBoundariesIncludeUnderMinLength() {
    List<String> values = provider.stringBoundaries(strSchema(5, 20));
    assertThat(values).anyMatch(s -> s.length() == 4); // minLength - 1
  }

  // ── Array boundaries ──────────────────────────────────────────────────────

  @Test
  void arrayBoundariesIncludeEmpty() {
    List<Integer> sizes = provider.arrayBoundaries(arraySchema(2, 5));
    assertThat(sizes).contains(0);
  }

  @Test
  void arrayBoundariesIncludeMinItemsAndMaxItems() {
    List<Integer> sizes = provider.arrayBoundaries(arraySchema(2, 5));
    assertThat(sizes).contains(2, 5);
  }

  @Test
  void arrayBoundariesIncludeOverMaxItems() {
    List<Integer> sizes = provider.arrayBoundaries(arraySchema(2, 5));
    assertThat(sizes).contains(6); // maxItems + 1
  }

  @Test
  void arrayBoundariesIncludeUnderMinItems() {
    List<Integer> sizes = provider.arrayBoundaries(arraySchema(2, 5));
    assertThat(sizes).contains(1); // minItems - 1
  }
}
