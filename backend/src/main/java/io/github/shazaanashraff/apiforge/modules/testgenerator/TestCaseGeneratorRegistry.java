package io.github.shazaanashraff.apiforge.modules.testgenerator;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class TestCaseGeneratorRegistry {

  private final Map<TestCategory, TestCaseGenerator> generators = new EnumMap<>(TestCategory.class);

  public TestCaseGeneratorRegistry() {
    register(new HappyPathGenerator());
    register(new BoundaryGenerator());
    register(new NegativeGenerator());
    register(new AuthGenerator());
    register(new SecurityGenerator());
    register(new IdempotencyGenerator());
    register(new RateLimitGenerator());
    register(new PerformanceSlaGenerator());
    register(new PayloadSizeGenerator());
    register(new PaginationGenerator());
    register(new MongoSpecificGenerator());
  }

  private void register(TestCaseGenerator gen) {
    generators.put(gen.category(), gen);
  }

  public List<TestCase> generateAll(TestGenerationContext ctx) {
    List<TestCase> all = new ArrayList<>();
    for (TestCaseGenerator gen : generators.values()) {
      all.addAll(gen.generate(ctx));
    }
    return all;
  }

  public List<TestCase> generate(TestGenerationContext ctx, List<TestCategory> categories) {
    List<TestCase> all = new ArrayList<>();
    for (TestCategory cat : categories) {
      TestCaseGenerator gen = generators.get(cat);
      if (gen != null) all.addAll(gen.generate(ctx));
    }
    return all;
  }
}
