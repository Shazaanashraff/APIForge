package io.github.shazaanashraff.apiforge.modules.datagenerator;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Random;

/** Generates numeric values respecting minimum, maximum, multipleOf, and type constraints. */
class NumberGenerator {

  private final Random random;

  NumberGenerator(Random random) {
    this.random = random;
  }

  Number generate(JsonNode schema) {
    String type = schema.path("type").asText("integer");
    boolean isInteger = "integer".equals(type) || "int32".equals(schema.path("format").asText());

    double minimum = schema.path("minimum").asDouble(isInteger ? 0 : 0.0);
    double maximum = schema.path("maximum").asDouble(isInteger ? 1_000_000 : 1_000_000.0);
    if (maximum < minimum) maximum = minimum + (isInteger ? 100 : 100.0);

    if (isInteger) {
      long min = (long) minimum;
      long max = (long) maximum;
      long range = max - min;
      long value = range > 0 ? min + (long) (random.nextDouble() * range) : min;
      return applyMultipleOf(value, schema);
    }

    double range = maximum - minimum;
    double value = minimum + random.nextDouble() * range;
    return Math.round(value * 100.0) / 100.0;
  }

  /** Returns minimum - 1 for negative tests. */
  Number belowMinimum(JsonNode schema) {
    double min = schema.path("minimum").asDouble(0);
    return (long) (min - 1);
  }

  /** Returns maximum + 1 for negative tests. */
  Number aboveMaximum(JsonNode schema) {
    double max = schema.path("maximum").asDouble(1_000_000);
    return (long) (max + 1);
  }

  private long applyMultipleOf(long value, JsonNode schema) {
    JsonNode multipleOf = schema.get("multipleOf");
    if (multipleOf != null && multipleOf.asLong(1) > 1) {
      long m = multipleOf.asLong();
      long rounded = (value / m) * m;
      return rounded == 0 ? m : rounded;
    }
    return value;
  }
}
