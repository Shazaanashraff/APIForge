package io.github.shazaanashraff.apiforge.modules.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;

class KafkaTopicConfigTest {

  private final KafkaTopicConfig config = new KafkaTopicConfig();

  @Test
  void testRunStartedTopicHasCorrectName() {
    NewTopic topic = config.testRunStartedTopic();
    assertThat(topic.name()).isEqualTo(KafkaTopics.TEST_RUN_STARTED);
  }

  @Test
  void testCaseCompletedTopicHasCorrectName() {
    NewTopic topic = config.testCaseCompletedTopic();
    assertThat(topic.name()).isEqualTo(KafkaTopics.TEST_CASE_COMPLETED);
  }

  @Test
  void testRunFinishedTopicHasCorrectName() {
    NewTopic topic = config.testRunFinishedTopic();
    assertThat(topic.name()).isEqualTo(KafkaTopics.TEST_RUN_FINISHED);
  }

  @Test
  void loadMetricSampleTopicHasCorrectName() {
    NewTopic topic = config.loadMetricSampleTopic();
    assertThat(topic.name()).isEqualTo(KafkaTopics.LOAD_METRIC_SAMPLE);
  }

  @Test
  void allTopicNamesBelongToApiforgeNamespace() {
    assertThat(config.testRunStartedTopic().name()).startsWith("apiforge.");
    assertThat(config.testCaseCompletedTopic().name()).startsWith("apiforge.");
    assertThat(config.testRunFinishedTopic().name()).startsWith("apiforge.");
    assertThat(config.loadMetricSampleTopic().name()).startsWith("apiforge.");
  }
}
