package io.github.shazaanashraff.apiforge.modules.validator;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.shazaanashraff.apiforge.modules.executor.TestCaseResult;
import io.github.shazaanashraff.apiforge.modules.schemaparser.AuthRequirement;
import io.github.shazaanashraff.apiforge.modules.schemaparser.Endpoint;
import io.github.shazaanashraff.apiforge.modules.schemaparser.ResponseSchema;
import io.github.shazaanashraff.apiforge.modules.schemaparser.SlaHint;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

class ResponseValidatorServiceTest {

  private final ResponseValidatorService service = new ResponseValidatorService();

  private static TestCaseResult result(int status, long responseTimeMs, String body) {
    return new TestCaseResult(
        "tc-1", "/test", "GET", "HAPPY_PATH", status, responseTimeMs, Map.of(), body, true, null);
  }

  private static Endpoint endpoint(Map<Integer, ResponseSchema> responses, SlaHint sla) {
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
        sla,
        null,
        List.of());
  }

  @Test
  void allPassingChecksProducesPassedResult() {
    Endpoint ep = endpoint(Map.of(200, new ResponseSchema(200, "OK", null)), SlaHint.none());
    ValidationResult result = service.validate(new ValidationRequest(result(200, 100L, ""), ep));
    assertThat(result.passed()).isTrue();
    assertThat(result.violations()).isEmpty();
  }

  @Test
  void undeclaredStatusCodeFailsValidation() {
    Endpoint ep = endpoint(Map.of(200, new ResponseSchema(200, "OK", null)), SlaHint.none());
    ValidationResult result = service.validate(new ValidationRequest(result(500, 100L, ""), ep));
    assertThat(result.passed()).isFalse();
    assertThat(result.violations()).anyMatch(v -> v.type() == ViolationType.STATUS_CODE);
  }

  @Test
  void slaExceededFailsValidation() {
    Endpoint ep = endpoint(Map.of(200, new ResponseSchema(200, "OK", null)), new SlaHint(50L));
    ValidationResult result = service.validate(new ValidationRequest(result(200, 200L, ""), ep));
    assertThat(result.passed()).isFalse();
    assertThat(result.violations()).anyMatch(v -> v.type() == ViolationType.SLA);
  }

  @Test
  void schemaViolationFailsValidation() {
    String schema =
        "{\"type\":\"object\",\"required\":[\"id\"],\"properties\":{\"id\":{\"type\":\"string\"}}}";
    Endpoint ep = endpoint(Map.of(200, new ResponseSchema(200, "OK", schema)), SlaHint.none());
    ValidationResult result =
        service.validate(new ValidationRequest(result(200, 50L, "{\"name\":\"Alice\"}"), ep));
    assertThat(result.passed()).isFalse();
    assertThat(result.violations()).anyMatch(v -> v.type() == ViolationType.RESPONSE_SCHEMA);
  }
}
