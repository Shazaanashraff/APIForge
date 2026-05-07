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

class StatusCodeValidatorTest {

  private static Endpoint endpointWithResponses(Map<Integer, ResponseSchema> responses) {
    return new Endpoint(
        "/test",
        HttpMethod.GET,
        null,
        null,
        List.of(),
        null,
        responses,
        AuthRequirement.NONE,
        null,
        null,
        SlaHint.none(),
        null,
        List.of());
  }

  @Test
  void declaredStatusCodeProducesNoViolations() {
    Endpoint endpoint = endpointWithResponses(Map.of(200, new ResponseSchema(200, "OK", null)));
    List<ValidationViolation> violations = StatusCodeValidator.validate(200, endpoint);
    assertThat(violations).isEmpty();
  }

  @Test
  void undeclaredStatusCodeProducesViolation() {
    Endpoint endpoint = endpointWithResponses(Map.of(200, new ResponseSchema(200, "OK", null)));
    List<ValidationViolation> violations = StatusCodeValidator.validate(404, endpoint);
    assertThat(violations).hasSize(1);
    assertThat(violations.get(0).type()).isEqualTo(ViolationType.STATUS_CODE);
  }

  @Test
  void emptyResponsesMapProducesNoViolations() {
    Endpoint endpoint = endpointWithResponses(Map.of());
    List<ValidationViolation> violations = StatusCodeValidator.validate(500, endpoint);
    assertThat(violations).isEmpty();
  }
}
