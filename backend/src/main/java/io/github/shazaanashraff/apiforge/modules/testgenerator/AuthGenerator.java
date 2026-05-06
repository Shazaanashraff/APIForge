package io.github.shazaanashraff.apiforge.modules.testgenerator;

import io.github.shazaanashraff.apiforge.modules.schemaparser.AuthRequirement;
import io.github.shazaanashraff.apiforge.modules.schemaparser.Endpoint;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class AuthGenerator implements TestCaseGenerator {

  @Override
  public TestCategory category() {
    return TestCategory.AUTH;
  }

  @Override
  public List<TestCase> generate(TestGenerationContext ctx) {
    Endpoint ep = ctx.endpoint();
    if (ep.authRequirement() == null || ep.authRequirement() == AuthRequirement.NONE) {
      return List.of();
    }

    String desc = ep.method() + " " + ep.path();
    return List.of(
        // 1. No token → 401
        new TestCase(
            UUID.randomUUID().toString(),
            TestCategory.AUTH,
            ep.path(),
            ep.method(),
            Map.of(),
            Map.of(),
            Map.of(),
            null,
            List.of(Assertion.statusCode(401)),
            "Auth: no token → 401 for " + desc,
            false),
        // 2. Expired token → 401
        new TestCase(
            UUID.randomUUID().toString(),
            TestCategory.AUTH,
            ep.path(),
            ep.method(),
            Map.of("Authorization", "Bearer expired.token.here"),
            Map.of(),
            Map.of(),
            null,
            List.of(Assertion.statusCode(401)),
            "Auth: expired token → 401 for " + desc,
            false),
        // 3. Wrong scope → 403
        new TestCase(
            UUID.randomUUID().toString(),
            TestCategory.AUTH,
            ep.path(),
            ep.method(),
            Map.of("Authorization", "Bearer wrong.scope.token"),
            Map.of(),
            Map.of(),
            null,
            List.of(Assertion.statusCode(403)),
            "Auth: wrong scope → 403 for " + desc,
            false));
  }
}
