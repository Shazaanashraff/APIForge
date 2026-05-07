package io.github.shazaanashraff.apiforge.modules.validator;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.shazaanashraff.apiforge.modules.schemaparser.AuthRequirement;
import io.github.shazaanashraff.apiforge.modules.schemaparser.Endpoint;
import io.github.shazaanashraff.apiforge.modules.schemaparser.ResponseSchema;
import io.github.shazaanashraff.apiforge.modules.schemaparser.SlaHint;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

class JsonBodyValidatorTest {

  private static final String SCHEMA_WITH_REQUIRED_ID =
      "{\"type\":\"object\",\"required\":[\"id\"],\"properties\":{\"id\":{\"type\":\"string\"}}}";

  private static Endpoint endpointWithSchema(String schemaJson) {
    return new Endpoint(
        "/test",
        HttpMethod.GET,
        null,
        null,
        List.of(),
        null,
        Map.of(200, new ResponseSchema(200, "OK", schemaJson)),
        AuthRequirement.NONE,
        null,
        null,
        SlaHint.none(),
        null,
        List.of());
  }

  @Test
  void validBodyAgainstSchemaPasses() {
    Endpoint endpoint = endpointWithSchema(SCHEMA_WITH_REQUIRED_ID);
    List<ValidationViolation> violations =
        JsonBodyValidator.validate(200, "{\"id\":\"abc\"}", endpoint);
    assertThat(violations).isEmpty();
  }

  @Test
  void invalidBodyMissingRequiredFieldProducesViolation() {
    Endpoint endpoint = endpointWithSchema(SCHEMA_WITH_REQUIRED_ID);
    List<ValidationViolation> violations =
        JsonBodyValidator.validate(200, "{\"name\":\"Alice\"}", endpoint);
    assertThat(violations).isNotEmpty();
    assertThat(violations.get(0).type()).isEqualTo(ViolationType.RESPONSE_SCHEMA);
  }

  @Test
  void nullSchemaJsonProducesNoViolations() {
    Endpoint endpoint = endpointWithSchema(null);
    List<ValidationViolation> violations =
        JsonBodyValidator.validate(200, "{\"id\":\"abc\"}", endpoint);
    assertThat(violations).isEmpty();
  }

  @Test
  void nonJsonBodyProducesViolation() {
    Endpoint endpoint = endpointWithSchema(SCHEMA_WITH_REQUIRED_ID);
    List<ValidationViolation> violations = JsonBodyValidator.validate(200, "not-json", endpoint);
    assertThat(violations).hasSize(1);
    assertThat(violations.get(0).message()).containsIgnoringCase("not valid JSON");
  }
}
