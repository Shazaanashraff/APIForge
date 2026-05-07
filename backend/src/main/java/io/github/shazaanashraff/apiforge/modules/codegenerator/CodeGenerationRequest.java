package io.github.shazaanashraff.apiforge.modules.codegenerator;

import io.github.shazaanashraff.apiforge.modules.testgenerator.TestCase;
import java.util.List;

/** Input to the code generator: a list of test cases plus metadata about the target environment. */
public record CodeGenerationRequest(
    List<TestCase> testCases, String baseUrl, String className, CodeFormat format) {}
