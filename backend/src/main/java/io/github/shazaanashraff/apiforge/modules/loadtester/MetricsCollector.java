package io.github.shazaanashraff.apiforge.modules.loadtester;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

class MetricsCollector {

  private final List<LoadSample> samples = new CopyOnWriteArrayList<>();

  void record(LoadSample sample) {
    samples.add(sample);
  }

  long totalRequests() {
    return samples.size();
  }

  long errorCount() {
    return samples.stream().filter(LoadSample::error).count();
  }

  List<LoadSample> snapshot() {
    return List.copyOf(samples);
  }

  LoadTestResult summarize(String testRunId, long durationMs) {
    long total = samples.size();
    long errors = samples.stream().filter(LoadSample::error).count();
    double errorRate = total > 0 ? (double) errors / total : 0.0;
    List<Long> sorted =
        samples.stream().map(LoadSample::responseTimeMs).sorted().collect(Collectors.toList());
    long p50 = PercentileCalculator.compute(sorted, 50);
    long p95 = PercentileCalculator.compute(sorted, 95);
    long p99 = PercentileCalculator.compute(sorted, 99);
    long max = sorted.isEmpty() ? 0L : sorted.get(sorted.size() - 1);
    return new LoadTestResult(testRunId, total, errors, errorRate, p50, p95, p99, max, durationMs);
  }
}
