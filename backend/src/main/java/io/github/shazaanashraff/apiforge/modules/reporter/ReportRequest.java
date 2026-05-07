package io.github.shazaanashraff.apiforge.modules.reporter;

import io.github.shazaanashraff.apiforge.modules.executor.ExecutionResult;
import io.github.shazaanashraff.apiforge.modules.validator.ValidationResult;
import java.util.List;

public record ReportRequest(
    ExecutionResult result, List<ValidationResult> validations, ReportFormat format) {}
