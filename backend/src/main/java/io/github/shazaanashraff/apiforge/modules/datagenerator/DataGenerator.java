package io.github.shazaanashraff.apiforge.modules.datagenerator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.shazaanashraff.apiforge.modules.schemaparser.RequestBodySchema;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;

/**
 * Schema-aware, seedable test data generator.
 *
 * <p>Create with a fixed seed for deterministic test runs (same seed → same data):
 * <pre>{@code
 * DataGenerator gen = new DataGenerator(42L);
 * Map<String, Object> body = gen.generatePayload(endpoint.requestBody());
 * }</pre>
 *
 * <p>Create without a seed for random data:
 * <pre>{@code
 * DataGenerator gen = new DataGenerator();
 * }</pre>
 *
 * <p>This class is NOT a Spring bean — callers construct it with the seed they need.
 * The test case generator (S07) is responsible for choosing seeds.
 */
@Slf4j
public class DataGenerator {

  private final long seed;
  private final Random random;
  private final Faker faker;
  private final ObjectMapper objectMapper;

  private final StringGenerator stringGen;
  private final NumberGenerator numberGen;
  private final ArrayGenerator arrayGen;
  private final MongoObjectIdGenerator objectIdGen;

  public DataGenerator(long seed) {
    this.seed = seed;
    this.random = new Random(seed);
    this.faker = new Faker(new Random(seed));
    this.objectMapper = new ObjectMapper();
    this.stringGen = new StringGenerator(new Random(seed), faker);
    this.numberGen = new NumberGenerator(new Random(seed));
    this.arrayGen = new ArrayGenerator(new Random(seed), this);
    this.objectIdGen = new MongoObjectIdGenerator(new Random(seed));
  }

  public DataGenerator() {
    this(System.nanoTime());
  }

  public long getSeed() {
    return seed;
  }

  public MongoObjectIdGenerator objectIds() {
    return objectIdGen;
  }

  // ── Main generation API ──────────────────────────────────────────────────

  /**
   * Generates a single valid value conforming to the given JSON Schema node.
   * Returns {@code null} when schema is null or empty.
   */
  public Object generateValidValue(JsonNode schema) {
    if (schema == null || schema.isNull() || schema.isMissingNode()) {
      return faker.lorem().word();
    }

    // enum — always pick a declared value
    JsonNode enumNode = schema.get("enum");
    if (enumNode != null && enumNode.isArray() && enumNode.size() > 0) {
      return enumNode.get(random.nextInt(enumNode.size())).asText();
    }

    // const
    JsonNode constNode = schema.get("const");
    if (constNode != null) {
      return constNode.asText();
    }

    String type = schema.path("type").asText("string");
    return switch (type) {
      case "string"           -> stringGen.generate(schema);
      case "integer", "number"-> numberGen.generate(schema);
      case "boolean"          -> random.nextBoolean();
      case "array"            -> arrayGen.generate(schema);
      case "object"           -> generateObject(schema);
      default                 -> faker.lorem().word();
    };
  }

  /**
   * Generates boundary values for a schema field:
   * <ul>
   *   <li>Strings: empty, at minLength, at maxLength, over maxLength
   *   <li>Numbers: at min, at max, below min, above max
   *   <li>Arrays: empty, at minItems, at maxItems, over maxItems
   * </ul>
   */
  public List<Object> generateBoundaryValues(JsonNode schema) {
    if (schema == null) return List.of();
    String type = schema.path("type").asText("string");
    BoundaryValueProvider bvp = new BoundaryValueProvider();
    return switch (type) {
      case "integer", "number" ->
          bvp.integerBoundaries(schema).stream().map(v -> (Object) v).toList();
      case "string" ->
          bvp.stringBoundaries(schema).stream().map(v -> (Object) v).toList();
      case "array" -> {
        List<Integer> sizes = bvp.arrayBoundaries(schema);
        List<Object> result = new ArrayList<>();
        for (int size : sizes) {
          List<Object> arr = new ArrayList<>();
          JsonNode items = schema.path("items");
          for (int i = 0; i < size; i++) {
            arr.add(generateValidValue(items.isMissingNode() ? null : items));
          }
          result.add(arr);
        }
        yield result;
      }
      default -> List.of(generateValidValue(schema), null, "");
    };
  }

  /**
   * Generates a value that violates the schema type (wrong type, null, or a known bad value).
   * Useful for negative test cases.
   */
  public Object generateInvalidValue(JsonNode schema) {
    if (schema == null) return null;
    String type = schema.path("type").asText("string");
    return switch (type) {
      case "string"  -> random.nextInt(10000);        // number instead of string
      case "integer" -> faker.lorem().word();         // string instead of number
      case "boolean" -> "maybe";                      // not a boolean
      case "array"   -> faker.lorem().word();         // string instead of array
      case "object"  -> "not-an-object";
      default        -> null;
    };
  }

  /**
   * Generates a complete request body as a {@code Map<String, Object>} from a
   * {@link RequestBodySchema}. Returns an empty map when the schema is null or unparseable.
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> generatePayload(RequestBodySchema requestBodySchema) {
    if (requestBodySchema == null || requestBodySchema.schemaJson() == null) {
      return Map.of();
    }
    try {
      JsonNode schema = objectMapper.readTree(requestBodySchema.schemaJson());
      Object value = generateValidValue(schema);
      if (value instanceof Map<?, ?> map) {
        return (Map<String, Object>) map;
      }
      return Map.of("value", value != null ? value : "");
    } catch (Exception e) {
      log.warn("Could not parse requestBody schema JSON: {}", e.getMessage());
      return Map.of();
    }
  }

  /**
   * Generates a payload that is {@code multiplier} times the declared max size.
   * Used for oversized-payload security/robustness tests.
   */
  public byte[] generateOversizedPayload(RequestBodySchema requestBodySchema, int multiplier) {
    if (requestBodySchema == null) {
      return new byte[0];
    }
    // Build a base payload then repeat it
    String base = faker.lorem().characters(1024);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < multiplier; i++) {
      sb.append(base);
    }
    return sb.toString().getBytes(StandardCharsets.UTF_8);
  }

  // ── Private helpers ──────────────────────────────────────────────────────

  Map<String, Object> generateObject(JsonNode schema) {
    Map<String, Object> result = new HashMap<>();
    JsonNode properties = schema.path("properties");
    if (properties.isMissingNode() || !properties.isObject()) {
      return result;
    }
    JsonNode required = schema.path("required");
    properties.fields().forEachRemaining(entry -> {
      String fieldName = entry.getKey();
      JsonNode fieldSchema = entry.getValue();
      // Always generate required fields; skip optional ones 30% of the time for variety
      boolean isRequired = required.isArray() && required.toString().contains("\"" + fieldName + "\"");
      if (isRequired || random.nextDouble() > 0.3) {
        result.put(fieldName, generateValidValue(fieldSchema));
      }
    });
    return result;
  }
}
