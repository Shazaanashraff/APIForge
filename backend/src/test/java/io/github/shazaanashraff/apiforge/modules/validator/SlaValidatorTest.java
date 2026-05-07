package io.github.shazaanashraff.apiforge.modules.validator;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.shazaanashraff.apiforge.modules.schemaparser.SlaHint;
import java.util.List;
import org.junit.jupiter.api.Test;

class SlaValidatorTest {

  @Test
  void responseWithinSlaProducesNoViolations() {
    List<ValidationViolation> violations = SlaValidator.validate(200L, new SlaHint(500L));
    assertThat(violations).isEmpty();
  }

  @Test
  void responseExceedingSlaProducesViolation() {
    List<ValidationViolation> violations = SlaValidator.validate(600L, new SlaHint(500L));
    assertThat(violations).hasSize(1);
    assertThat(violations.get(0).type()).isEqualTo(ViolationType.SLA);
    assertThat(violations.get(0).message()).contains("600ms").contains("500ms");
  }

  @Test
  void noSlaHintProducesNoViolations() {
    List<ValidationViolation> violations = SlaValidator.validate(9999L, SlaHint.none());
    assertThat(violations).isEmpty();
  }
}
