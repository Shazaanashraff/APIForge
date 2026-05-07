package io.github.shazaanashraff.apiforge.modules.validator;

import io.github.shazaanashraff.apiforge.modules.schemaparser.SlaHint;
import java.util.ArrayList;
import java.util.List;

class SlaValidator {

  private SlaValidator() {}

  static List<ValidationViolation> validate(long responseTimeMs, SlaHint slaHint) {
    List<ValidationViolation> violations = new ArrayList<>();
    if (slaHint == null || !slaHint.hasSla()) {
      return violations;
    }
    if (responseTimeMs > slaHint.thresholdMs()) {
      violations.add(
          new ValidationViolation(
              ViolationType.SLA,
              "responseTimeMs",
              "Response time "
                  + responseTimeMs
                  + "ms exceeded SLA threshold "
                  + slaHint.thresholdMs()
                  + "ms"));
    }
    return violations;
  }
}
