package io.github.shazaanashraff.apiforge.modules.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class KafkaEventsTest {

  @Test
  void testRunStartedEventFieldsAreAccessible() {
    TestRunStartedEvent event =
        new TestRunStartedEvent("run-1", "proj-1", "tenant-1", "Petstore API", 1000L, 35);
    assertThat(event.testRunId()).isEqualTo("run-1");
    assertThat(event.specName()).isEqualTo("Petstore API");
    assertThat(event.totalTestCases()).isEqualTo(35);
    assertThat(event.startedAt()).isEqualTo(1000L);
  }

  @Test
  void testCaseCompletedEventCarriesPassedFlag() {
    TestCaseCompletedEvent passed =
        new TestCaseCompletedEvent(
            "run-1", "tc-1", "/pets/{id}", "GET", "HAPPY_PATH", 200, 45L, true, null, 2000L);
    TestCaseCompletedEvent failed =
        new TestCaseCompletedEvent(
            "run-1",
            "tc-2",
            "/pets/{id}",
            "GET",
            "NEGATIVE",
            500,
            120L,
            false,
            "Expected 4xx",
            3000L);

    assertThat(passed.passed()).isTrue();
    assertThat(passed.failureReason()).isNull();
    assertThat(failed.passed()).isFalse();
    assertThat(failed.failureReason()).isEqualTo("Expected 4xx");
  }

  @Test
  void testRunFinishedEventHasCorrectCounts() {
    TestRunFinishedEvent event =
        new TestRunFinishedEvent("run-1", "proj-1", "tenant-1", 10, 8, 2, 0, 5000L);
    assertThat(event.passed() + event.failed() + event.skipped()).isEqualTo(event.totalTestCases());
    assertThat(event.passed()).isEqualTo(8);
    assertThat(event.failed()).isEqualTo(2);
  }

  @Test
  void loadMetricSampleEventHasLatencyPercentiles() {
    LoadMetricSampleEvent event =
        new LoadMetricSampleEvent("run-1", 3000L, 10, 50.0, 0.02, 120L, 350L, 800L);
    assertThat(event.p50Ms()).isLessThan(event.p95Ms());
    assertThat(event.p95Ms()).isLessThan(event.p99Ms());
    assertThat(event.errorRate()).isBetween(0.0, 1.0);
  }
}
