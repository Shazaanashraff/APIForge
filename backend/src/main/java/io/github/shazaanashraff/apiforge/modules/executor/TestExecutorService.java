package io.github.shazaanashraff.apiforge.modules.executor;

import io.github.shazaanashraff.apiforge.modules.kafka.TestCaseCompletedEvent;
import io.github.shazaanashraff.apiforge.modules.kafka.TestRunEventPublisher;
import io.github.shazaanashraff.apiforge.modules.kafka.TestRunFinishedEvent;
import io.github.shazaanashraff.apiforge.modules.kafka.TestRunStartedEvent;
import io.github.shazaanashraff.apiforge.modules.testgenerator.Assertion;
import io.github.shazaanashraff.apiforge.modules.testgenerator.TestCase;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TestExecutorService {

  private final WebClient client;
  private final Optional<TestRunEventPublisher> publisher;

  public TestExecutorService(
      WebClient.Builder webClientBuilder, Optional<TestRunEventPublisher> publisher) {
    this.client = webClientBuilder.build();
    this.publisher = publisher;
  }

  public Mono<ExecutionResult> executeAll(ExecutionRequest request) {
    long startedAt = System.currentTimeMillis();
    Map<String, String> authHeaders =
        request.authToken() != null
            ? AuthHeaderProvider.headersFor(AuthRequirement.BEARER_JWT, request.authToken())
            : Map.of();

    publisher.ifPresent(
        p ->
            p.publishTestRunStarted(
                new TestRunStartedEvent(
                    request.testRunId(),
                    request.projectId(),
                    request.tenantId(),
                    "APIForge test run",
                    startedAt,
                    request.testCases().size())));

    return Flux.fromIterable(request.testCases())
        .flatMap(tc -> executeSingle(tc, request, authHeaders), request.config().concurrency())
        .collectList()
        .map(
            results -> {
              long finishedAt = System.currentTimeMillis();
              int passed = (int) results.stream().filter(TestCaseResult::passed).count();
              int failed = results.size() - passed;

              publisher.ifPresent(
                  p ->
                      p.publishTestRunFinished(
                          new TestRunFinishedEvent(
                              request.testRunId(),
                              request.projectId(),
                              request.tenantId(),
                              results.size(),
                              passed,
                              failed,
                              0,
                              finishedAt)));

              return new ExecutionResult(
                  request.testRunId(), results, passed, failed, 0, startedAt, finishedAt);
            });
  }

  private Mono<TestCaseResult> executeSingle(
      TestCase tc, ExecutionRequest request, Map<String, String> authHeaders) {
    VariableStore vars = new VariableStore();
    long[] startMs = {System.currentTimeMillis()};

    return HttpRequestBuilder.build(client, tc, request.baseUrl(), authHeaders, vars)
        .exchangeToMono(
            response -> {
              long responseTimeMs = System.currentTimeMillis() - startMs[0];
              int statusCode = response.statusCode().value();
              Map<String, String> headers = new LinkedHashMap<>();
              response
                  .headers()
                  .asHttpHeaders()
                  .forEach((k, v) -> headers.put(k, String.join(",", v)));
              return response
                  .bodyToMono(String.class)
                  .defaultIfEmpty("")
                  .map(
                      body -> {
                        String failureReason =
                            evaluate(tc, statusCode, responseTimeMs, headers, body);
                        boolean passed = failureReason == null;
                        publishTestCaseCompleted(
                            request, tc, statusCode, responseTimeMs, passed, failureReason);
                        return new TestCaseResult(
                            tc.id(),
                            tc.endpointPath(),
                            tc.method().name(),
                            tc.category().name(),
                            statusCode,
                            responseTimeMs,
                            headers,
                            body,
                            passed,
                            failureReason);
                      });
            })
        .onErrorResume(
            ex -> {
              long responseTimeMs = System.currentTimeMillis() - startMs[0];
              String reason = "Request error: " + ex.getMessage();
              publishTestCaseCompleted(request, tc, 0, responseTimeMs, false, reason);
              return Mono.just(
                  new TestCaseResult(
                      tc.id(),
                      tc.endpointPath(),
                      tc.method().name(),
                      tc.category().name(),
                      0,
                      responseTimeMs,
                      Map.of(),
                      "",
                      false,
                      reason));
            });
  }

  private void publishTestCaseCompleted(
      ExecutionRequest request,
      TestCase tc,
      int statusCode,
      long responseTimeMs,
      boolean passed,
      String failureReason) {
    publisher.ifPresent(
        p ->
            p.publishTestCaseCompleted(
                new TestCaseCompletedEvent(
                    request.testRunId(),
                    tc.id(),
                    tc.endpointPath(),
                    tc.method().name(),
                    tc.category().name(),
                    statusCode,
                    responseTimeMs,
                    passed,
                    failureReason,
                    System.currentTimeMillis())));
  }

  private String evaluate(
      TestCase tc, int statusCode, long responseTimeMs, Map<String, String> headers, String body) {
    for (Assertion a : tc.expectedAssertions()) {
      String failure = checkAssertion(a, statusCode, responseTimeMs, headers);
      if (failure != null) return failure;
    }
    return null;
  }

  private String checkAssertion(
      Assertion a, int statusCode, long responseTimeMs, Map<String, String> headers) {
    return switch (a.type()) {
      case STATUS_CODE -> {
        int expected = Integer.parseInt(a.value());
        yield statusCode == expected
            ? null
            : "Expected status " + expected + " but got " + statusCode;
      }
      case STATUS_CODE_RANGE -> {
        int base = Integer.parseInt(a.value().substring(0, 1)) * 100;
        yield statusCode >= base && statusCode < base + 100
            ? null
            : "Expected status in range " + a.value() + " but got " + statusCode;
      }
      case HEADER_PRESENT -> {
        String key = a.key().toLowerCase(Locale.ROOT);
        boolean found =
            headers.keySet().stream().anyMatch(h -> h.toLowerCase(Locale.ROOT).equals(key));
        yield found ? null : "Expected header '" + a.key() + "' to be present";
      }
      case HEADER_VALUE -> {
        String key = a.key().toLowerCase(Locale.ROOT);
        String actual =
            headers.entrySet().stream()
                .filter(e -> e.getKey().toLowerCase(Locale.ROOT).equals(key))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
        yield a.value().equals(actual)
            ? null
            : "Expected header '"
                + a.key()
                + "' to be '"
                + a.value()
                + "' but was '"
                + actual
                + "'";
      }
      case RESPONSE_TIME_MS -> {
        long threshold = Long.parseLong(a.value());
        yield responseTimeMs <= threshold
            ? null
            : "Response time " + responseTimeMs + "ms exceeded threshold " + threshold + "ms";
      }
      case RESPONSE_SCHEMA -> null;
    };
  }
}
