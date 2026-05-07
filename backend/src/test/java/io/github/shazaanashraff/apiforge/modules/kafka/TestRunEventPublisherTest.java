package io.github.shazaanashraff.apiforge.modules.kafka;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class TestRunEventPublisherTest {

  @Mock KafkaTemplate<String, Object> kafkaTemplate;

  @InjectMocks TestRunEventPublisher publisher;

  @Test
  void publishTestRunStartedSendsToCorrectTopic() {
    TestRunStartedEvent event =
        new TestRunStartedEvent("run-1", "proj-1", "tenant-1", "Petstore", 1000L, 10);
    publisher.publishTestRunStarted(event);
    verify(kafkaTemplate).send(KafkaTopics.TEST_RUN_STARTED, "run-1", event);
  }

  @Test
  void publishTestCaseCompletedSendsToCorrectTopic() {
    TestCaseCompletedEvent event =
        new TestCaseCompletedEvent(
            "run-1", "tc-1", "/pets/{id}", "GET", "HAPPY_PATH", 200, 45L, true, null, 2000L);
    publisher.publishTestCaseCompleted(event);
    verify(kafkaTemplate).send(KafkaTopics.TEST_CASE_COMPLETED, "run-1", event);
  }

  @Test
  void publishTestRunFinishedSendsToCorrectTopic() {
    TestRunFinishedEvent event =
        new TestRunFinishedEvent("run-1", "proj-1", "tenant-1", 10, 8, 2, 0, 5000L);
    publisher.publishTestRunFinished(event);
    verify(kafkaTemplate).send(KafkaTopics.TEST_RUN_FINISHED, "run-1", event);
  }

  @Test
  void publishLoadMetricSampleSendsToCorrectTopic() {
    LoadMetricSampleEvent event =
        new LoadMetricSampleEvent("run-1", 3000L, 10, 50.0, 0.02, 120L, 350L, 800L);
    publisher.publishLoadMetricSample(event);
    verify(kafkaTemplate).send(KafkaTopics.LOAD_METRIC_SAMPLE, "run-1", event);
  }
}
