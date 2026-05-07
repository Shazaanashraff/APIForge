package io.github.shazaanashraff.apiforge.modules.executor;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.shazaanashraff.apiforge.modules.testgenerator.Assertion;
import io.github.shazaanashraff.apiforge.modules.testgenerator.TestCase;
import io.github.shazaanashraff.apiforge.modules.testgenerator.TestCategory;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

class HttpRequestBuilderTest {

  private static TestCase minimalGet(String path) {
    return new TestCase(
        "tc-1",
        TestCategory.HAPPY_PATH,
        path,
        HttpMethod.GET,
        null,
        null,
        null,
        null,
        List.of(Assertion.statusCode(200)),
        "test",
        false);
  }

  @Test
  void buildUriIncludesQueryParams() {
    TestCase tc =
        new TestCase(
            "tc-1",
            TestCategory.HAPPY_PATH,
            "/items",
            HttpMethod.GET,
            null,
            Map.of("page", "1"),
            null,
            null,
            List.of(),
            "test",
            false);
    String uri = HttpRequestBuilder.buildUri(tc, "http://localhost:8080", new VariableStore());
    assertThat(uri).contains("page=1");
  }

  @Test
  void buildHeadersMergesTestCaseAndAuthHeaders() {
    TestCase tc =
        new TestCase(
            "tc-1",
            TestCategory.HAPPY_PATH,
            "/items",
            HttpMethod.GET,
            Map.of("X-Custom", "value"),
            null,
            null,
            null,
            List.of(),
            "test",
            false);
    Map<String, String> auth = Map.of("Authorization", "Bearer tok");
    Map<String, String> headers = HttpRequestBuilder.buildHeaders(tc, auth);
    assertThat(headers).containsEntry("Authorization", "Bearer tok");
    assertThat(headers).containsEntry("X-Custom", "value");
  }

  @Test
  void buildHeadersIncludesContentTypeForPostWithBody() {
    TestCase tc =
        new TestCase(
            "tc-1",
            TestCategory.HAPPY_PATH,
            "/items",
            HttpMethod.POST,
            null,
            null,
            null,
            Map.of("name", "test"),
            List.of(),
            "test",
            false);
    Map<String, String> headers = HttpRequestBuilder.buildHeaders(tc, Map.of());
    assertThat(headers).containsEntry("Content-Type", "application/json");
  }
}
