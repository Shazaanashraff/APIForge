package io.github.shazaanashraff.apiforge.modules.validator;

import io.github.shazaanashraff.apiforge.modules.schemaparser.Endpoint;
import java.util.ArrayList;
import java.util.List;

class StatusCodeValidator {

  private StatusCodeValidator() {}

  static List<ValidationViolation> validate(int actualStatusCode, Endpoint endpoint) {
    List<ValidationViolation> violations = new ArrayList<>();
    if (endpoint.responses() == null || endpoint.responses().isEmpty()) {
      return violations;
    }
    boolean hasDeclaredDefault = endpoint.responses().containsKey(-1);
    boolean matchesDeclared = endpoint.responses().containsKey(actualStatusCode);
    if (!matchesDeclared && !hasDeclaredDefault) {
      violations.add(
          new ValidationViolation(
              ViolationType.STATUS_CODE,
              "statusCode",
              "Response status "
                  + actualStatusCode
                  + " is not declared in the spec. Declared: "
                  + endpoint.responses().keySet()));
    }
    return violations;
  }
}
