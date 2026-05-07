package io.github.shazaanashraff.apiforge.modules.sse;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/runs")
public class SseController {

  private final RedisMessageListenerContainer listenerContainer;

  public SseController(RedisMessageListenerContainer listenerContainer) {
    this.listenerContainer = listenerContainer;
  }

  @GetMapping(value = "/{runId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter streamEvents(@PathVariable String runId) {
    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
    ChannelTopic topic = new ChannelTopic("progress:" + runId);

    MessageListener listener =
        (Message message, byte[] pattern) -> {
          try {
            emitter.send(SseEmitter.event().data(new String(message.getBody())));
          } catch (Exception e) {
            emitter.completeWithError(e);
          }
        };

    listenerContainer.addMessageListener(listener, topic);

    Runnable cleanup = () -> listenerContainer.removeMessageListener(listener, topic);
    emitter.onCompletion(cleanup);
    emitter.onTimeout(cleanup);
    emitter.onError(t -> cleanup.run());

    return emitter;
  }
}
