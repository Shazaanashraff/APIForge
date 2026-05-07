package io.github.shazaanashraff.apiforge.modules.loadtester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class MetricsCollectorTest {

  @Test
  void recordIncrementsTotalRequests() {
    MetricsCollector collector = new MetricsCollector();
    collector.record(new LoadSample(0L, 100L, 200, false));
    collector.record(new LoadSample(0L, 200L, 200, false));
    assertThat(collector.totalRequests()).isEqualTo(2L);
  }

  @Test
  void errorCountMatchesOnlyErrorSamples() {
    MetricsCollector collector = new MetricsCollector();
    collector.record(new LoadSample(0L, 50L, 200, false));
    collector.record(new LoadSample(0L, 50L, 500, true));
    collector.record(new LoadSample(0L, 50L, 503, true));
    assertThat(collector.errorCount()).isEqualTo(2L);
  }

  @Test
  void summarizeComputesErrorRate() {
    MetricsCollector collector = new MetricsCollector();
    collector.record(new LoadSample(0L, 100L, 200, false));
    collector.record(new LoadSample(0L, 100L, 500, true));
    LoadTestResult result = collector.summarize("run-1", 1000L);
    assertThat(result.errorRate()).isCloseTo(0.5, within(0.001));
    assertThat(result.totalRequests()).isEqualTo(2L);
    assertThat(result.errorCount()).isEqualTo(1L);
  }

  @Test
  void summarizeComputesPercentilesFromResponseTimes() {
    MetricsCollector collector = new MetricsCollector();
    for (int i = 1; i <= 10; i++) {
      collector.record(new LoadSample(0L, i * 10L, 200, false));
    }
    LoadTestResult result = collector.summarize("run-1", 5000L);
    assertThat(result.p50Ms()).isEqualTo(50L);
    assertThat(result.maxMs()).isEqualTo(100L);
  }
}
