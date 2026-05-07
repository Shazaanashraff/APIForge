package io.github.shazaanashraff.apiforge.modules.api;

import io.github.shazaanashraff.apiforge.modules.executor.ExecutionConfig;
import io.github.shazaanashraff.apiforge.modules.executor.ExecutionRequest;
import io.github.shazaanashraff.apiforge.modules.executor.ExecutionResult;
import io.github.shazaanashraff.apiforge.modules.executor.TestExecutorService;
import io.github.shazaanashraff.apiforge.modules.schemaparser.Endpoint;
import io.github.shazaanashraff.apiforge.modules.schemaparser.ParsedSpec;
import io.github.shazaanashraff.apiforge.modules.schemaparser.SpecIngestionService;
import io.github.shazaanashraff.apiforge.modules.testgenerator.TestCase;
import io.github.shazaanashraff.apiforge.modules.testgenerator.TestCaseGeneratorRegistry;
import io.github.shazaanashraff.apiforge.modules.testgenerator.TestGenerationContext;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/runs")
public class TestRunController {

  private final SpecIngestionService specIngestionService;
  private final TestCaseGeneratorRegistry registry;
  private final TestExecutorService executorService;

  public TestRunController(
      SpecIngestionService specIngestionService,
      TestCaseGeneratorRegistry registry,
      TestExecutorService executorService) {
    this.specIngestionService = specIngestionService;
    this.registry = registry;
    this.executorService = executorService;
  }

  @PostMapping
  public ResponseEntity<ExecutionResult> run(@RequestBody RunTestsRequest request) {
    ParsedSpec spec = specIngestionService.ingestUrl(request.specUrl());

    List<TestCase> testCases = new ArrayList<>();
    for (Endpoint endpoint : spec.endpoints()) {
      testCases.addAll(registry.generateAll(TestGenerationContext.of(endpoint)));
    }

    String runId = request.testRunId() != null ? request.testRunId() : UUID.randomUUID().toString();
    ExecutionRequest execRequest =
        new ExecutionRequest(
            testCases,
            request.baseUrl(),
            runId,
            request.projectId(),
            request.tenantId(),
            request.authToken(),
            ExecutionConfig.defaults());

    ExecutionResult result = executorService.executeAll(execRequest).block();
    return ResponseEntity.ok(result);
  }
}
