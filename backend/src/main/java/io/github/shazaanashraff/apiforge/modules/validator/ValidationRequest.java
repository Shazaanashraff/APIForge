package io.github.shazaanashraff.apiforge.modules.validator;

import io.github.shazaanashraff.apiforge.modules.executor.TestCaseResult;
import io.github.shazaanashraff.apiforge.modules.schemaparser.Endpoint;

public record ValidationRequest(TestCaseResult result, Endpoint endpoint) {}
