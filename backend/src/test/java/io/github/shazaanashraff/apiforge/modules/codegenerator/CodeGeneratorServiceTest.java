package io.github.shazaanashraff.apiforge.modules.codegenerator;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.shazaanashraff.apiforge.modules.testgenerator.Assertion;
import io.github.shazaanashraff.apiforge.modules.testgenerator.TestCase;
import io.github.shazaanashraff.apiforge.modules.testgenerator.TestCategory;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

class CodeGeneratorServiceTest {

  private final CodeGeneratorService service = new CodeGeneratorService();

  @Test
  void generateForRestAssuredReturnsNonEmptyResult() {
    CodeGenerationResult result = service.generate(request(CodeFormat.JUNIT5_REST_ASSURED));
    assertThat(result.files()).isNotEmpty();
    assertThat(result.format()).isEqualTo(CodeFormat.JUNIT5_REST_ASSURED);
  }

  @Test
  void generateForK6ReturnsNonEmptyResult() {
    CodeGenerationResult result = service.generate(request(CodeFormat.K6));
    assertThat(result.files()).isNotEmpty();
    assertThat(result.format()).isEqualTo(CodeFormat.K6);
  }

  @Test
  void generateForJestReturnsPackageJsonFile() {
    CodeGenerationResult result = service.generate(request(CodeFormat.JEST_SUPERTEST));
    assertThat(result.files()).anyMatch(f -> f.name().equals("package.json"));
  }

  @Test
  void generateForGatlingReturnsSimulationJavaFile() {
    CodeGenerationResult result = service.generate(request(CodeFormat.GATLING));
    assertThat(result.files()).anyMatch(f -> f.name().endsWith("Simulation.java"));
  }

  @Test
  void generateZipIsNonEmpty() throws IOException {
    byte[] zip = service.generateZip(request(CodeFormat.JUNIT5_REST_ASSURED));
    assertThat(zip).isNotEmpty();
  }

  @Test
  void generateZipContainsPomXml() throws IOException {
    byte[] zip = service.generateZip(request(CodeFormat.JUNIT5_REST_ASSURED));
    String content = new String(zip);
    assertThat(content).contains("pom.xml");
  }

  private CodeGenerationRequest request(CodeFormat format) {
    TestCase tc =
        new TestCase(
            "tc001",
            TestCategory.HAPPY_PATH,
            "/items/{id}",
            HttpMethod.GET,
            Map.of(),
            Map.of(),
            Map.of("id", "test-id"),
            null,
            List.of(Assertion.statusCodeRange("2xx")),
            "Happy path test",
            false);
    return new CodeGenerationRequest(List.of(tc), "https://api.example.com", "ItemApi", format);
  }
}
