package io.github.shazaanashraff.apiforge.modules.testgenerator;

import java.util.List;

interface TestCaseGenerator {
  TestCategory category();

  List<TestCase> generate(TestGenerationContext ctx);
}
