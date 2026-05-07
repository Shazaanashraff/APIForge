package io.github.shazaanashraff.apiforge.modules.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Produces lifecycle events to the APIForge Kafka topics.
 *
 * <p>The bean is only created when {@link KafkaTemplate} is available (i.e. Kafka
 * auto-configuration is active). This prevents context-load failures in test profiles that exclude
 * {@code KafkaAutoConfiguration}.
 *
 * <p>Callers: executor module (test-case lifecycle), load-tester module (metrics).
 */
@Service
@ConditionalOnBean(KafkaTemplate.class)
public class TestRunEventPublisher {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  public TestRunEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void publishTestRunStarted(TestRunStartedEvent event) {
    kafkaTemplate.send(KafkaTopics.TEST_RUN_STARTED, event.testRunId(), event);
  }

  public void publishTestCaseCompleted(TestCaseCompletedEvent event) {
    kafkaTemplate.send(KafkaTopics.TEST_CASE_COMPLETED, event.testRunId(), event);
  }

  public void publishTestRunFinished(TestRunFinishedEvent event) {
    kafkaTemplate.send(KafkaTopics.TEST_RUN_FINISHED, event.testRunId(), event);
  }

  public void publishLoadMetricSample(LoadMetricSampleEvent event) {
    kafkaTemplate.send(KafkaTopics.LOAD_METRIC_SAMPLE, event.testRunId(), event);
  }
}
