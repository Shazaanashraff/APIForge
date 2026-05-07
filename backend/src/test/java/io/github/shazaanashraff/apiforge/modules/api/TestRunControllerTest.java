package io.github.shazaanashraff.apiforge.modules.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.shazaanashraff.apiforge.modules.executor.ExecutionResult;
import io.github.shazaanashraff.apiforge.modules.executor.TestExecutorService;
import io.github.shazaanashraff.apiforge.modules.schemaparser.ParsedSpec;
import io.github.shazaanashraff.apiforge.modules.schemaparser.SpecIngestionService;
import io.github.shazaanashraff.apiforge.modules.schemaparser.SpecParseException;
import io.github.shazaanashraff.apiforge.modules.testgenerator.TestCaseGeneratorRegistry;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class TestRunControllerTest {

  @Mock private SpecIngestionService specIngestionService;
  @Mock private TestCaseGeneratorRegistry registry;
  @Mock private TestExecutorService executorService;
  @InjectMocks private TestRunController controller;

  private MockMvc mvc;

  @BeforeEach
  void setup() {
    mvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  void runReturns200WithExecutionResult() throws Exception {
    ParsedSpec spec = ParsedSpec.of(List.of(), "test", "3.0.x", null);
    ExecutionResult result = new ExecutionResult("run-1", List.of(), 0, 0, 0, 1000L, 2000L);

    when(specIngestionService.ingestUrl(any())).thenReturn(spec);
    when(executorService.executeAll(any())).thenReturn(Mono.just(result));

    mvc.perform(
            post("/api/runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"specUrl\":\"http://example.com/openapi.json\","
                        + "\"baseUrl\":\"http://localhost:8080\","
                        + "\"projectId\":\"proj-1\",\"tenantId\":\"tenant-1\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.testRunId").value("run-1"));
  }

  @Test
  void runReturns422WhenSpecCannotBeParsed() throws Exception {
    when(specIngestionService.ingestUrl(any()))
        .thenThrow(new SpecParseException("not found", null));

    mvc.perform(
            post("/api/runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"specUrl\":\"http://bad.url/spec\","
                        + "\"baseUrl\":\"http://localhost:8080\","
                        + "\"projectId\":\"p\",\"tenantId\":\"t\"}"))
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  void runUsesProvidedTestRunId() throws Exception {
    ParsedSpec spec = ParsedSpec.of(List.of(), "test", "3.0.x", null);
    ExecutionResult result = new ExecutionResult("my-run-id", List.of(), 0, 0, 0, 0L, 100L);

    when(specIngestionService.ingestUrl(any())).thenReturn(spec);
    when(executorService.executeAll(any())).thenReturn(Mono.just(result));

    mvc.perform(
            post("/api/runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"specUrl\":\"http://example.com/api.json\","
                        + "\"baseUrl\":\"http://sut\","
                        + "\"testRunId\":\"my-run-id\","
                        + "\"projectId\":\"p\",\"tenantId\":\"t\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.testRunId").value("my-run-id"));
  }
}
