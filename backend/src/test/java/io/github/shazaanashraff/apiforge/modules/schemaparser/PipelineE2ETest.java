package io.github.shazaanashraff.apiforge.modules.schemaparser;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.shazaanashraff.apiforge.modules.executor.ExecutionRequest;
import io.github.shazaanashraff.apiforge.modules.executor.ExecutionResult;
import io.github.shazaanashraff.apiforge.modules.executor.TestExecutorService;
import io.github.shazaanashraff.apiforge.modules.testgenerator.TestCase;
import io.github.shazaanashraff.apiforge.modules.testgenerator.TestCaseGeneratorRegistry;
import io.github.shazaanashraff.apiforge.modules.testgenerator.TestGenerationContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * End-to-end pipeline test: parses a live OpenAPI spec, generates test cases, executes them
 * against a mock "buggy" target API, and asserts that violations are detected.
 *
 * <p>Placed in the schemaparser package for package-private access to {@link EndpointMapper}.
 * No Spring context is loaded — all service objects are instantiated directly.
 */
class PipelineE2ETest {

  private static final String SPEC_JSON =
      """
      {
        "openapi": "3.0.3",
        "info": { "title": "Sample Buggy API", "version": "1.0.0" },
        "paths": {
          "/items/{id}": {
            "get": {
              "operationId": "getItem",
              "parameters": [{
                "name": "id",
                "in": "path",
                "required": true,
                "schema": { "type": "string" }
              }],
              "responses": {
                "200": { "description": "OK" },
                "400": { "description": "Bad Request — invalid id format" },
                "404": { "description": "Not Found" }
              }
            }
          }
        }
      }
      """;

  static MockWebServer server;
  static SpecIngestionService specIngestion;
  static TestCaseGeneratorRegistry registry;
  static TestExecutorService executorService;

  @BeforeAll
  static void setUp() throws Exception {
    server = new MockWebServer();
    server.setDispatcher(
        new Dispatcher() {
          @Override
          public MockResponse dispatch(RecordedRequest request) {
            if ("/openapi.json".equals(request.getPath())) {
              return new MockResponse()
                  .setResponseCode(200)
                  .addHeader("Content-Type", "application/json")
                  .setBody(SPEC_JSON);
            }
            // Buggy target: always 200 regardless of input — negative tests expecting 400 will fail
            return new MockResponse().setResponseCode(200).setBody("{\"id\":\"item-1\"}");
          }
        });
    server.start();

    ObjectMapper objectMapper = new ObjectMapper();
    PaginationHintDetector paginationDetector = new PaginationHintDetector();
    IdFormatDetector idFormatDetector = new IdFormatDetector();
    EndpointMapper endpointMapper =
        new EndpointMapper(paginationDetector, idFormatDetector, objectMapper);
    OpenApiParser openApiParser = new OpenApiParser(endpointMapper);
    PostmanParser postmanParser =
        new PostmanParser(objectMapper, paginationDetector, idFormatDetector);

    specIngestion = new SpecIngestionService(openApiParser, postmanParser);
    registry = new TestCaseGeneratorRegistry();
    executorService = new TestExecutorService(WebClient.builder(), Optional.empty());
  }

  @AfterAll
  static void tearDown() throws Exception {
    server.shutdown();
  }

  @Test
  void specParsesEndpointFromMockServer() {
    ParsedSpec spec = specIngestion.ingestUrl(server.url("/openapi.json").toString());

    assertThat(spec.endpoints()).hasSize(1);
    assertThat(spec.endpoints().get(0).path()).isEqualTo("/items/{id}");
    assertThat(spec.endpoints().get(0).method().name()).isEqualTo("GET");
  }

  @Test
  void fullPipelineDetectsBuggyApiReturningSameStatusForAllInputs() {
    String specUrl = server.url("/openapi.json").toString();
    String baseUrl = server.url("").toString().replaceAll("/$", "");

    ParsedSpec spec = specIngestion.ingestUrl(specUrl);
    assertThat(spec.endpoints()).isNotEmpty();

    List<TestCase> testCases = new ArrayList<>();
    for (Endpoint ep : spec.endpoints()) {
      testCases.addAll(registry.generateAll(TestGenerationContext.of(ep)));
    }
    assertThat(testCases).isNotEmpty();

    ExecutionResult result =
        executorService
            .executeAll(ExecutionRequest.of(testCases, baseUrl, "e2e-run-1", "proj-e2e", "t1"))
            .block();

    assertThat(result).isNotNull();
    assertThat(result.testRunId()).isEqualTo("e2e-run-1");
    // Negative cases assert 400 for invalid id; buggy API returns 200 → those cases fail
    assertThat(result.failed()).isGreaterThan(0);
    assertThat(result.results())
        .anyMatch(r -> "NEGATIVE".equals(r.category()) && !r.passed());
  }

  @Test
  void happyPathCasesPassWhenApiResponds200() {
    String specUrl = server.url("/openapi.json").toString();
    String baseUrl = server.url("").toString().replaceAll("/$", "");

    ParsedSpec spec = specIngestion.ingestUrl(specUrl);

    List<TestCase> happyPathOnly =
        spec.endpoints().stream()
            .flatMap(
                ep ->
                    registry
                        .generate(
                            TestGenerationContext.of(ep),
                            List.of(
                                io.github.shazaanashraff.apiforge.modules.testgenerator.TestCategory
                                    .HAPPY_PATH))
                        .stream())
            .toList();

    assertThat(happyPathOnly).isNotEmpty();

    ExecutionResult result =
        executorService
            .executeAll(
                ExecutionRequest.of(happyPathOnly, baseUrl, "e2e-happy-1", "proj-e2e", "t1"))
            .block();

    assertThat(result).isNotNull();
    // Happy-path asserts 2xx; mock returns 200 → all pass
    assertThat(result.passed()).isEqualTo(happyPathOnly.size());
    assertThat(result.failed()).isZero();
  }
}
