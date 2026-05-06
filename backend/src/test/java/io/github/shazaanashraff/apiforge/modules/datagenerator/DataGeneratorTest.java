package io.github.shazaanashraff.apiforge.modules.datagenerator;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.shazaanashraff.apiforge.modules.schemaparser.RequestBodySchema;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DataGeneratorTest {

  private static final long SEED = 42L;

  private DataGenerator generator() {
    return new DataGenerator(SEED);
  }

  private static ObjectNode schema(String type) {
    ObjectMapper om = new ObjectMapper();
    ObjectNode n = om.createObjectNode();
    n.put("type", type);
    return n;
  }

  // ── Seedability ───────────────────────────────────────────────────────────

  @Test
  void sameSeedProducesSameString() {
    ObjectNode stringSchema = schema("string");
    stringSchema.put("minLength", 5);
    stringSchema.put("maxLength", 20);

    Object v1 = new DataGenerator(SEED).generateValidValue(stringSchema);
    Object v2 = new DataGenerator(SEED).generateValidValue(stringSchema);

    assertThat(v1).isEqualTo(v2);
  }

  @Test
  void sameSeedProducesSameInteger() {
    ObjectNode intSchema = schema("integer");
    intSchema.put("minimum", 1);
    intSchema.put("maximum", 100);

    Object v1 = new DataGenerator(SEED).generateValidValue(intSchema);
    Object v2 = new DataGenerator(SEED).generateValidValue(intSchema);

    assertThat(v1).isEqualTo(v2);
  }

  @Test
  void differentSeedsProduceDifferentValues() {
    ObjectNode intSchema = schema("integer");
    intSchema.put("minimum", 0);
    intSchema.put("maximum", 1_000_000);

    // With a huge range, different seeds almost certainly produce different values
    Object v1 = new DataGenerator(1L).generateValidValue(intSchema);
    Object v2 = new DataGenerator(999_999L).generateValidValue(intSchema);

    assertThat(v1).isNotEqualTo(v2);
  }

  // ── Type correctness ──────────────────────────────────────────────────────

  @Test
  void generatesBooleanForBooleanType() {
    Object value = generator().generateValidValue(schema("boolean"));
    assertThat(value).isInstanceOf(Boolean.class);
  }

  @Test
  void generatesListForArrayType() {
    ObjectNode arraySchema = schema("array");
    arraySchema.put("minItems", 2);
    arraySchema.put("maxItems", 5);
    ObjectNode items = arraySchema.putObject("items");
    items.put("type", "string");

    Object value = generator().generateValidValue(arraySchema);

    assertThat(value).isInstanceOf(List.class);
    assertThat((List<?>) value).hasSizeBetween(2, 5);
  }

  @Test
  void generatesMapForObjectType() {
    ObjectMapper om = new ObjectMapper();
    ObjectNode objectSchema = om.createObjectNode();
    objectSchema.put("type", "object");
    ObjectNode props = objectSchema.putObject("properties");
    props.putObject("name").put("type", "string");
    props.putObject("age").put("type", "integer");
    objectSchema.putArray("required").add("name").add("age");

    Object value = generator().generateValidValue(objectSchema);

    assertThat(value).isInstanceOf(Map.class);
    @SuppressWarnings("unchecked")
    Map<String, Object> map = (Map<String, Object>) value;
    assertThat(map).containsKey("name").containsKey("age");
  }

  @Test
  void generatesEnumValue() {
    ObjectMapper om = new ObjectMapper();
    ObjectNode enumSchema = om.createObjectNode();
    enumSchema.put("type", "string");
    enumSchema.putArray("enum").add("available").add("pending").add("sold");

    Object value = generator().generateValidValue(enumSchema);

    assertThat(value).isIn("available", "pending", "sold");
  }

  // ── Format handling ───────────────────────────────────────────────────────

  @Test
  void generatesEmailFormat() {
    ObjectNode emailSchema = schema("string");
    emailSchema.put("format", "email");

    Object value = generator().generateValidValue(emailSchema);

    assertThat(value.toString()).contains("@");
  }

  @Test
  void generatesUuidFormat() {
    ObjectNode uuidSchema = schema("string");
    uuidSchema.put("format", "uuid");

    Object value = generator().generateValidValue(uuidSchema);

    assertThat(value.toString())
        .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
  }

  // ── Boundary values ──────────────────────────────────────────────────────

  @Test
  void integerBoundaryValuesIncludeMinAndMax() {
    ObjectMapper om = new ObjectMapper();
    ObjectNode intSchema = om.createObjectNode();
    intSchema.put("type", "integer");
    intSchema.put("minimum", 5);
    intSchema.put("maximum", 10);

    List<Object> boundaries = generator().generateBoundaryValues(intSchema);

    assertThat(boundaries).contains(5L, 10L, 4L, 11L);
  }

  // ── Invalid values ────────────────────────────────────────────────────────

  @Test
  void invalidValueForStringIsNumber() {
    Object invalid = generator().generateInvalidValue(schema("string"));
    assertThat(invalid).isInstanceOf(Number.class);
  }

  @Test
  void invalidValueForIntegerIsString() {
    Object invalid = generator().generateInvalidValue(schema("integer"));
    assertThat(invalid).isInstanceOf(String.class);
  }

  // ── generatePayload ───────────────────────────────────────────────────────

  @Test
  void generatePayloadFromNullReturnsEmptyMap() {
    Map<String, Object> payload = generator().generatePayload(null);
    assertThat(payload).isEmpty();
  }

  @Test
  void generatePayloadFromSchemaWithNoSchemaJsonReturnsEmptyMap() {
    RequestBodySchema rbs = new RequestBodySchema(true, "application/json", null);
    assertThat(generator().generatePayload(rbs)).isEmpty();
  }

  @Test
  void generatePayloadFromObjectSchema() {
    String schemaJson = """
        {"type":"object","properties":{"name":{"type":"string"},"count":{"type":"integer"}},
         "required":["name","count"]}""";
    RequestBodySchema rbs = new RequestBodySchema(true, "application/json", schemaJson);

    Map<String, Object> payload = generator().generatePayload(rbs);

    assertThat(payload).containsKeys("name", "count");
    assertThat(payload.get("name")).isInstanceOf(String.class);
    assertThat(payload.get("count")).isInstanceOf(Number.class);
  }
}
