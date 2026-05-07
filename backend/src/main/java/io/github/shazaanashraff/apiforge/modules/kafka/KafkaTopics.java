package io.github.shazaanashraff.apiforge.modules.kafka;

/** Canonical Kafka topic names used by producers and consumers. */
public final class KafkaTopics {

  public static final String TEST_RUN_STARTED = "apiforge.test-run.started";
  public static final String TEST_CASE_COMPLETED = "apiforge.test-case.completed";
  public static final String TEST_RUN_FINISHED = "apiforge.test-run.finished";
  public static final String LOAD_METRIC_SAMPLE = "apiforge.load-metric.sample";

  private KafkaTopics() {}
}
