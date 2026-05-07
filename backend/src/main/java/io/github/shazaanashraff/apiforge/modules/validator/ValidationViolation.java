package io.github.shazaanashraff.apiforge.modules.validator;

public record ValidationViolation(ViolationType type, String field, String message) {}
