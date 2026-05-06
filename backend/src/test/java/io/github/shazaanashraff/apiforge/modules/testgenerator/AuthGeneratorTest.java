package io.github.shazaanashraff.apiforge.modules.testgenerator;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.shazaanashraff.apiforge.modules.schemaparser.AuthRequirement;
import io.github.shazaanashraff.apiforge.modules.schemaparser.Endpoint;
import io.github.shazaanashraff.apiforge.modules.schemaparser.IdFormatHint;
import io.github.shazaanashraff.apiforge.modules.schemaparser.PaginationHint;
import io.github.shazaanashraff.apiforge.modules.schemaparser.PayloadSizeHint;
import io.github.shazaanashraff.apiforge.modules.schemaparser.ResponseSchema;
import io.github.shazaanashraff.apiforge.modules.schemaparser.SlaHint;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

class AuthGeneratorTest {

  private final AuthGenerator generator = new AuthGenerator();

  @Test
  void securedEndpointGeneratesExactlyThreeCases() {
    List<TestCase> cases = generator.generate(TestGenerationContext.of(securedEndpoint()));
    assertThat(cases).hasSize(3);
  }

  @Test
  void firstCaseHasNoTokenAndExpects401() {
    List<TestCase> cases = generator.generate(TestGenerationContext.of(securedEndpoint()));

    TestCase noToken = cases.get(0);
    assertThat(noToken.headers()).doesNotContainKey("Authorization");
    assertThat(noToken.expectedAssertions())
        .anyMatch(
            a ->
                a.type() == Assertion.AssertionType.STATUS_CODE && a.value().equals("401"));
  }

  @Test
  void secondCaseHasExpiredTokenAndExpects401() {
    List<TestCase> cases = generator.generate(TestGenerationContext.of(securedEndpoint()));

    TestCase expiredToken = cases.get(1);
    assertThat(expiredToken.headers()).containsKey("Authorization");
    assertThat(expiredToken.expectedAssertions())
        .anyMatch(
            a ->
                a.type() == Assertion.AssertionType.STATUS_CODE && a.value().equals("401"));
  }

  @Test
  void thirdCaseHasWrongScopeAndExpects403() {
    List<TestCase> cases = generator.generate(TestGenerationContext.of(securedEndpoint()));

    TestCase wrongScope = cases.get(2);
    assertThat(wrongScope.expectedAssertions())
        .anyMatch(
            a ->
                a.type() == Assertion.AssertionType.STATUS_CODE && a.value().equals("403"));
  }

  @Test
  void unsecuredEndpointGeneratesNoCases() {
    Endpoint ep =
        new Endpoint(
            "/public",
            HttpMethod.GET,
            "publicOp",
            "Public",
            List.of(),
            null,
            Map.of(200, new ResponseSchema(200, "OK", null)),
            AuthRequirement.NONE,
            PaginationHint.none(),
            PayloadSizeHint.unknown(),
            SlaHint.none(),
            IdFormatHint.unknown(),
            List.of());

    assertThat(generator.generate(TestGenerationContext.of(ep))).isEmpty();
  }

  @Test
  void allCasesHaveAuthCategory() {
    List<TestCase> cases = generator.generate(TestGenerationContext.of(securedEndpoint()));
    assertThat(cases).allMatch(tc -> tc.category() == TestCategory.AUTH);
  }

  private Endpoint securedEndpoint() {
    return new Endpoint(
        "/api/resource",
        HttpMethod.DELETE,
        "deleteResource",
        "Delete resource",
        List.of(),
        null,
        Map.of(204, new ResponseSchema(204, "No Content", null)),
        AuthRequirement.BEARER_JWT,
        PaginationHint.none(),
        PayloadSizeHint.unknown(),
        SlaHint.none(),
        IdFormatHint.unknown(),
        List.of());
  }
}
