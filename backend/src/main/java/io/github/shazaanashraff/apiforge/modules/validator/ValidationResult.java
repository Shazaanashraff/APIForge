package io.github.shazaanashraff.apiforge.modules.validator;

import java.util.List;

public record ValidationResult(
    String testCaseId, boolean passed, List<ValidationViolation> violations) {}
