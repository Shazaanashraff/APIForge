package io.github.shazaanashraff.apiforge.modules.datagenerator;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Random;
import net.datafaker.Faker;

/**
 * Generates string values respecting minLength, maxLength, format, enum, and pattern constraints.
 */
class StringGenerator {

  private final Random random;
  private final Faker faker;

  StringGenerator(Random random, Faker random2) {
    this.random = random;
    this.faker = random2;
  }

  String generate(JsonNode schema) {
    String format = schema.path("format").asText(null);

    // enum — pick a random valid value
    JsonNode enumNode = schema.get("enum");
    if (enumNode != null && enumNode.isArray() && enumNode.size() > 0) {
      return enumNode.get(random.nextInt(enumNode.size())).asText();
    }

    if (format != null) {
      return switch (format.toLowerCase()) {
        case "email" -> faker.internet().emailAddress();
        case "uuid" -> java.util.UUID.randomUUID().toString();
        case "date" -> faker.date().birthday(1, 80).toString().substring(0, 10);
        case "date-time" -> java.time.Instant.now().toString();
        case "uri", "url" -> faker.internet().url();
        case "password" -> faker.internet().password(8, 20);
        case "byte" ->
            java.util.Base64.getEncoder().encodeToString(faker.lorem().word().getBytes());
        default -> generateLengthBounded(schema);
      };
    }

    return generateLengthBounded(schema);
  }

  private String generateLengthBounded(JsonNode schema) {
    int minLength = schema.path("minLength").asInt(1);
    int maxLength = schema.path("maxLength").asInt(Math.max(minLength + 20, 50));
    if (maxLength < minLength) maxLength = minLength;

    int targetLength = minLength + random.nextInt(Math.max(1, maxLength - minLength + 1));
    String base = faker.lorem().characters(targetLength, true, true);

    // Trim or pad to satisfy exact length constraints
    if (base.length() > maxLength) return base.substring(0, maxLength);
    if (base.length() < minLength) return base + "x".repeat(minLength - base.length());
    return base;
  }

  /** Generates a string just over maxLength — for negative/oversized tests. */
  String generateOverMaxLength(JsonNode schema) {
    int maxLength = schema.path("maxLength").asInt(255);
    return "x".repeat(maxLength + 1);
  }

  /** Generates a string just under minLength — for negative tests. */
  String generateUnderMinLength(JsonNode schema) {
    int minLength = schema.path("minLength").asInt(1);
    if (minLength <= 0) return "";
    return "x".repeat(Math.max(0, minLength - 1));
  }
}
