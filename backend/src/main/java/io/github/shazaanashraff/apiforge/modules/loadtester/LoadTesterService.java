package io.github.shazaanashraff.apiforge.modules.loadtester;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.shazaanashraff.apiforge.modules.kafka.LoadMetricSampleEvent;
import io.github.shazaanashraff.apiforge.modules.kafka.TestRunEventPublisher;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class LoadTesterService {

  private static final HttpClient HTTP_CLIENT =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final LoadMetricRepository metricsRepo;
  private final Optional<TestRunEventPublisher> publisher;

  public LoadTesterService(
      LoadMetricRepository metricsRepo, Optional<TestRunEventPublisher> publisher) {
    this.metricsRepo = metricsRepo;
    this.publisher = publisher;
  }

  public LoadTestResult runLoadTest(LoadScenario scenario) throws InterruptedException {
    MetricsCollector collector = new MetricsCollector();
    long startMs = System.currentTimeMillis();
    long endMs = startMs + (scenario.durationSeconds() * 1000L);
    long rampIntervalMs =
        scenario.rampUpSeconds() > 0 && scenario.virtualUsers() > 0
            ? (scenario.rampUpSeconds() * 1000L) / scenario.virtualUsers()
            : 0L;

    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      for (int i = 0; i < scenario.virtualUsers(); i++) {
        final long delay = rampIntervalMs * i;
        executor.submit(
            () -> {
              if (delay > 0) {
                try {
                  Thread.sleep(delay);
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                  return;
                }
              }
              while (System.currentTimeMillis() < endMs
                  && !Thread.currentThread().isInterrupted()) {
                collector.record(executeRequest(scenario));
              }
            });
      }

      while (System.currentTimeMillis() < endMs) {
        Thread.sleep(1_000);
        if (System.currentTimeMillis() < endMs) {
          persistSample(scenario, collector, System.currentTimeMillis());
        }
      }
    }

    long durationMs = System.currentTimeMillis() - startMs;
    LoadTestResult result = collector.summarize(scenario.testRunId(), durationMs);
    publishFinalSample(scenario, result);
    return result;
  }

  private LoadSample executeRequest(LoadScenario scenario) {
    long start = System.currentTimeMillis();
    try {
      HttpRequest.BodyPublisher body =
          scenario.requestBody() != null
              ? HttpRequest.BodyPublishers.ofString(
                  MAPPER.writeValueAsString(scenario.requestBody()))
              : HttpRequest.BodyPublishers.noBody();

      HttpRequest.Builder builder =
          HttpRequest.newBuilder()
              .uri(URI.create(scenario.baseUrl() + scenario.endpointPath()))
              .timeout(Duration.ofSeconds(30))
              .method(scenario.httpMethod().toUpperCase(), body);

      if (scenario.headers() != null) {
        scenario.headers().forEach(builder::header);
      }
      if (scenario.requestBody() != null
          && (scenario.headers() == null || !scenario.headers().containsKey("Content-Type"))) {
        builder.header("Content-Type", "application/json");
      }

      HttpResponse<Void> response =
          HTTP_CLIENT.send(builder.build(), HttpResponse.BodyHandlers.discarding());
      long responseTimeMs = System.currentTimeMillis() - start;
      int status = response.statusCode();
      return new LoadSample(start, responseTimeMs, status, status >= 500);
    } catch (Exception e) {
      return new LoadSample(start, System.currentTimeMillis() - start, 0, true);
    }
  }

  private void persistSample(LoadScenario scenario, MetricsCollector collector, long now) {
    List<Long> sorted =
        collector.snapshot().stream()
            .map(LoadSample::responseTimeMs)
            .sorted()
            .collect(Collectors.toList());
    long total = collector.totalRequests();
    long errors = collector.errorCount();
    double errRate = total > 0 ? (double) errors / total : 0.0;
    double rps = scenario.durationSeconds() > 0 ? (double) total / scenario.durationSeconds() : 0.0;

    LoadMetric metric = new LoadMetric();
    metric.setTenantId(UUID.fromString(scenario.tenantId()));
    metric.setTestRunId(UUID.fromString(scenario.testRunId()));
    metric.setSampledAt(Instant.ofEpochMilli(now));
    metric.setActiveVus(scenario.virtualUsers());
    metric.setRps(BigDecimal.valueOf(rps));
    metric.setP50Ms(PercentileCalculator.compute(sorted, 50));
    metric.setP95Ms(PercentileCalculator.compute(sorted, 95));
    metric.setP99Ms(PercentileCalculator.compute(sorted, 99));
    metric.setMaxMs(sorted.isEmpty() ? 0L : sorted.get(sorted.size() - 1));
    metric.setErrorRate(BigDecimal.valueOf(errRate));
    metric.setTotalRequests(total);
    metricsRepo.save(metric);
  }

  private void publishFinalSample(LoadScenario scenario, LoadTestResult result) {
    publisher.ifPresent(
        p ->
            p.publishLoadMetricSample(
                new LoadMetricSampleEvent(
                    scenario.testRunId(),
                    System.currentTimeMillis(),
                    scenario.virtualUsers(),
                    result.totalRequests() > 0
                        ? (double) result.totalRequests() / result.durationMs() * 1000
                        : 0.0,
                    result.errorRate(),
                    result.p50Ms(),
                    result.p95Ms(),
                    result.p99Ms())));
  }
}
