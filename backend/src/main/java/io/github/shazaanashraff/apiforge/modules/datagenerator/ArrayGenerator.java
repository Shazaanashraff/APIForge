package io.github.shazaanashraff.apiforge.modules.datagenerator;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Generates array values respecting minItems and maxItems constraints. */
class ArrayGenerator {

  private final Random random;
  private final DataGenerator parent;

  ArrayGenerator(Random random, DataGenerator parent) {
    this.random = random;
    this.parent = parent;
  }

  List<Object> generate(JsonNode schema) {
    int minItems = schema.path("minItems").asInt(0);
    int maxItems = schema.path("maxItems").asInt(Math.max(minItems + 5, 5));
    if (maxItems < minItems) maxItems = minItems;

    int count = minItems + random.nextInt(Math.max(1, maxItems - minItems + 1));
    JsonNode itemSchema = schema.path("items");

    List<Object> result = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      result.add(parent.generateValidValue(itemSchema.isMissingNode() ? null : itemSchema));
    }
    return result;
  }

  /** Returns a list with one item more than maxItems — for overflow tests. */
  List<Object> generateOverMax(JsonNode schema) {
    int maxItems = schema.path("maxItems").asInt(5);
    JsonNode itemSchema = schema.path("items");
    List<Object> result = new ArrayList<>(maxItems + 1);
    for (int i = 0; i <= maxItems; i++) {
      result.add(parent.generateValidValue(itemSchema.isMissingNode() ? null : itemSchema));
    }
    return result;
  }
}
