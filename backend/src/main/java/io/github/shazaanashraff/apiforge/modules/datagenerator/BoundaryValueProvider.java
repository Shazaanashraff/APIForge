package io.github.shazaanashraff.apiforge.modules.datagenerator;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import net.jqwik.api.Arbitraries;

/**
 * Produces boundary-value lists for a given JSON Schema field.
 *
 * <p>Boundary analysis covers: just-below-min, at-min, at-max, just-above-max, and typical
 * middle values. Used by the test case generator to create negative/edge-case tests.
 *
 * <p>JQwik's {@link Arbitraries} API generates random values within valid ranges,
 * complementing the deterministic boundary points.
 */
public class BoundaryValueProvider {

  /**
   * Returns boundary integers for a numeric schema:
   * {@code [min-1, min, min+1, mid, max-1, max, max+1]}.
   */
  public List<Long> integerBoundaries(JsonNode schema) {
    long min = schema.path("minimum").asLong(0);
    long max = schema.path("maximum").asLong(100);
    if (max < min) max = min + 100;

    long mid = Arbitraries.longs().between(min, max).sample();

    List<Long> values = new ArrayList<>();
    values.add(min - 1);      // below min — invalid
    values.add(min);          // at min — valid boundary
    if (min + 1 <= max) values.add(min + 1);  // just above min — valid
    values.add(mid);          // random valid middle
    if (max - 1 >= min) values.add(max - 1);  // just below max — valid
    values.add(max);          // at max — valid boundary
    values.add(max + 1);      // above max — invalid
    return values;
  }

  /**
   * Returns boundary strings for a string schema:
   * {@code ["", minLength-string, maxLength-string, (maxLength+1)-string]}.
   */
  public List<String> stringBoundaries(JsonNode schema) {
    int minLength = schema.path("minLength").asInt(0);
    int maxLength = schema.path("maxLength").asInt(255);
    if (maxLength < minLength) maxLength = minLength;

    String randomValid = Arbitraries.strings()
        .ofMinLength(minLength)
        .ofMaxLength(maxLength)
        .alpha()
        .sample();

    List<String> values = new ArrayList<>();
    values.add("");                              // empty — invalid if minLength > 0
    if (minLength > 1) values.add("a".repeat(minLength - 1)); // under min — invalid
    values.add("a".repeat(minLength));           // at min — valid boundary
    values.add(randomValid);                     // random valid
    values.add("a".repeat(maxLength));           // at max — valid boundary
    values.add("a".repeat(maxLength + 1));       // over max — invalid
    return values;
  }

  /**
   * Returns boundary array sizes for an array schema:
   * {@code [empty, minItems-1, minItems, maxItems, maxItems+1]}.
   */
  public List<Integer> arrayBoundaries(JsonNode schema) {
    int minItems = schema.path("minItems").asInt(0);
    int maxItems = schema.path("maxItems").asInt(10);
    if (maxItems < minItems) maxItems = minItems;

    List<Integer> sizes = new ArrayList<>();
    sizes.add(0);                                // empty — invalid if minItems > 0
    if (minItems > 0) sizes.add(minItems - 1);   // under min — invalid
    sizes.add(minItems);                         // at min — valid
    if (minItems != maxItems) sizes.add(maxItems); // at max — valid
    sizes.add(maxItems + 1);                     // over max — invalid
    return sizes;
  }
}
