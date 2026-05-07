package io.github.shazaanashraff.apiforge.modules.sse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
class SseControllerTest {

  @Mock RedisMessageListenerContainer listenerContainer;
  @InjectMocks SseController controller;

  @Test
  void streamEventsReturnsNonNullEmitter() {
    SseEmitter emitter = controller.streamEvents("run-abc");
    assertThat(emitter).isNotNull();
  }

  @Test
  void streamEventsRegistersListenerOnCorrectTopic() {
    controller.streamEvents("run-xyz");
    verify(listenerContainer).addMessageListener(any(), eq(new ChannelTopic("progress:run-xyz")));
  }

  @Test
  void streamEventsDifferentRunIdsUseDifferentTopics() {
    controller.streamEvents("run-1");
    controller.streamEvents("run-2");
    verify(listenerContainer).addMessageListener(any(), eq(new ChannelTopic("progress:run-1")));
    verify(listenerContainer).addMessageListener(any(), eq(new ChannelTopic("progress:run-2")));
  }
}
