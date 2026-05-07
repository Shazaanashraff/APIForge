package io.github.shazaanashraff.apiforge.modules.validator;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ResponseValidatorService {

  public ValidationResult validate(ValidationRequest request) {
    List<ValidationViolation> violations = new ArrayList<>();

    violations.addAll(
        StatusCodeValidator.validate(request.result().statusCode(), request.endpoint()));

    violations.addAll(
        JsonBodyValidator.validate(
            request.result().statusCode(), request.result().responseBody(), request.endpoint()));

    violations.addAll(
        SlaValidator.validate(request.result().responseTimeMs(), request.endpoint().slaHint()));

    return new ValidationResult(request.result().testCaseId(), violations.isEmpty(), violations);
  }
}
