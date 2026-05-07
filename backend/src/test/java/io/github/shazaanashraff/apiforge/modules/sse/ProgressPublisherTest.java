package io.github.shazaanashraff.apiforge.modules.sse;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

@ExtendWith(MockitoExtension.class)
class ProgressPublisherTest {

  @Mock StringRedisTemplate redis;
  @InjectMocks ProgressPublisher publisher;

  @Test
  void publishSendsToCorrectChannel() {
    ProgressEvent event = ProgressEvent.of("run-1", ProgressEventType.TEST_RUN_STARTED, "{}");
    publisher.publish("run-1", event);
    verify(redis).convertAndSend(eq("progress:run-1"), contains("run-1"));
  }

  @Test
  void publishSerializesEventTypeAsJson() {
    ProgressEvent event = ProgressEvent.of("run-2", ProgressEventType.TEST_CASE_COMPLETED, "ok");
    publisher.publish("run-2", event);
    verify(redis).convertAndSend(eq("progress:run-2"), contains("TEST_CASE_COMPLETED"));
  }

  @Test
  void publishIncludesPayloadInJson() {
    ProgressEvent event = ProgressEvent.of("run-3", ProgressEventType.TEST_RUN_FINISHED, "done");
    publisher.publish("run-3", event);
    verify(redis).convertAndSend(eq("progress:run-3"), contains("done"));
  }
}
