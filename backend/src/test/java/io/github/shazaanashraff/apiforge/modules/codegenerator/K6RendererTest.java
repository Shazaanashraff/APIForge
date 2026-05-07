package io.github.shazaanashraff.apiforge.modules.codegenerator;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.shazaanashraff.apiforge.modules.testgenerator.Assertion;
import io.github.shazaanashraff.apiforge.modules.testgenerator.TestCase;
import io.github.shazaanashraff.apiforge.modules.testgenerator.TestCategory;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

class K6RendererTest {

  private final K6Renderer renderer = new K6Renderer();

  @Test
  void k6ScriptContainsOptionsBlock() {
    CodeGenerationResult result = renderer.generate(request(List.of(simpleGet())));
    assertThat(k6Script(result))
        .contains("export const options")
        .contains("vus:")
        .contains("duration:");
  }

  @Test
  void getRequestUsesHttpGet() {
    CodeGenerationResult result = renderer.generate(request(List.of(simpleGet())));
    assertThat(k6Script(result)).contains("http.get(");
  }

  @Test
  void statusRangeIsRenderedAsJsCondition() {
    CodeGenerationResult result = renderer.generate(request(List.of(simpleGet())));
    assertThat(k6Script(result)).contains("r.status >= 200").contains("r.status < 300");
  }

  @Test
  void checkBlockIsPresentForEachTestCase() {
    CodeGenerationResult result = renderer.generate(request(List.of(simpleGet())));
    assertThat(k6Script(result)).contains("check(res0");
  }

  @Test
  void postWithBodyUsesJsonStringify() {
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
    CodeGenerationResult result = renderer.generate(request(List.of(post)));
    assertThat(k6Script(result)).contains("JSON.stringify(");
  }

  private String k6Script(CodeGenerationResult result) {
    return result.files().getFirst().content();
  }

  private CodeGenerationRequest request(List<TestCase> cases) {
    return new CodeGenerationRequest(
        cases, "https://api.example.com", "PetstoreApi", CodeFormat.K6);
  }

  private TestCase simpleGet() {
    return new TestCase(
        "tc001",
        TestCategory.HAPPY_PATH,
        "/pets/{id}",
        HttpMethod.GET,
        Map.of(),
        Map.of(),
        Map.of("id", "abc123"),
        null,
        List.of(Assertion.statusCodeRange("2xx")),
        "Happy path: valid pet ID",
        false);
  }
}
