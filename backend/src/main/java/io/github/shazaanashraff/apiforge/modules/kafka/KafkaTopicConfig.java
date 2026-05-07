package io.github.shazaanashraff.apiforge.modules.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/** Declares the four APIForge Kafka topics. Spring's KafkaAdmin creates them on startup. */
@Configuration
public class KafkaTopicConfig {

  @Bean
  public NewTopic testRunStartedTopic() {
    return TopicBuilder.name(KafkaTopics.TEST_RUN_STARTED).partitions(3).replicas(1).build();
  }

  @Bean
  public NewTopic testCaseCompletedTopic() {
    return TopicBuilder.name(KafkaTopics.TEST_CASE_COMPLETED).partitions(3).replicas(1).build();
  }

  @Bean
  public NewTopic testRunFinishedTopic() {
    return TopicBuilder.name(KafkaTopics.TEST_RUN_FINISHED).partitions(3).replicas(1).build();
  }

  @Bean
  public NewTopic loadMetricSampleTopic() {
    return TopicBuilder.name(KafkaTopics.LOAD_METRIC_SAMPLE).partitions(3).replicas(1).build();
  }
}
