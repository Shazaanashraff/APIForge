package io.github.shazaanashraff.apiforge.modules.codegenerator;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.shazaanashraff.apiforge.modules.testgenerator.Assertion;
import io.github.shazaanashraff.apiforge.modules.testgenerator.TestCase;
import io.github.shazaanashraff.apiforge.modules.testgenerator.TestCategory;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

class RestAssuredRendererTest {

  private final RestAssuredRenderer renderer = new RestAssuredRenderer();

  @Test
  void classNameAppearsInGeneratedCode() {
    CodeGenerationResult result = renderer.generate(request("PetstoreApi", List.of(simpleGet())));
    assertThat(javaCode(result)).contains("public class PetstoreApiTest");
  }

  @Test
  void baseUrlAppearsInSetupMethod() {
    CodeGenerationResult result = renderer.generate(request("PetstoreApi", List.of(simpleGet())));
    assertThat(javaCode(result)).contains("RestAssured.baseURI = \"https://api.example.com\"");
  }

  @Test
  void pathParamIsSubstitutedInUrl() {
    CodeGenerationResult result = renderer.generate(request("PetstoreApi", List.of(simpleGet())));
    assertThat(javaCode(result)).contains(".get(\"/pets/507f1f77bcf86cd799439011\")");
  }

  @Test
  void statusCode2xxUsesHamcrestRange() {
    CodeGenerationResult result = renderer.generate(request("PetstoreApi", List.of(simpleGet())));
    assertThat(javaCode(result)).contains("greaterThanOrEqualTo(200)").contains("lessThan(300)");
  }

  @Test
  void pomXmlFileIsIncluded() {
    CodeGenerationResult result = renderer.generate(request("PetstoreApi", List.of(simpleGet())));
    assertThat(result.files()).anyMatch(f -> f.name().equals("pom.xml"));
  }

  @Test
  void authorizationHeaderIsRendered() {
    CodeGenerationResult result = renderer.generate(request("PetstoreApi", List.of(simpleGet())));
    assertThat(javaCode(result)).contains(".header(\"Authorization\", \"Bearer token123\")");
  }

  @Test
  void postBodyIsRenderedAsJsonString() {
    TestCase post =
        new TestCase(
            "tc002",
            TestCategory.HAPPY_PATH,
            "/pets",
            HttpMethod.POST,
            Map.of(),
            Map.of(),
            Map.of(),
            Map.of("name", "Fluffy"),
            List.of(Assertion.statusCode(201)),
            "Create pet",
            false);
    CodeGenerationResult result = renderer.generate(request("PetstoreApi", List.of(post)));
    assertThat(javaCode(result)).contains(".contentType(ContentType.JSON)").contains(".body(");
  }

  private String javaCode(CodeGenerationResult result) {
    return result.files().stream()
        .filter(f -> f.name().endsWith(".java"))
        .findFirst()
        .map(GeneratedFile::content)
        .orElseThrow();
  }

  private CodeGenerationRequest request(String className, List<TestCase> cases) {
    return new CodeGenerationRequest(
        cases, "https://api.example.com", className, CodeFormat.JUNIT5_REST_ASSURED);
  }

  private TestCase simpleGet() {
    return new TestCase(
        "tc001",
        TestCategory.HAPPY_PATH,
        "/pets/{id}",
        HttpMethod.GET,
        Map.of("Authorization", "Bearer token123"),
        Map.of(),
        Map.of("id", "507f1f77bcf86cd799439011"),
        null,
        List.of(Assertion.statusCodeRange("2xx")),
        "Happy path: valid pet ID",
        false);
  }
}
