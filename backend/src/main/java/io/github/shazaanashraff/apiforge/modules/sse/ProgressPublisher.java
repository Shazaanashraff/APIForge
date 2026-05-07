package io.github.shazaanashraff.apiforge.modules.sse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProgressPublisher {

  private static final Logger log = LoggerFactory.getLogger(ProgressPublisher.class);
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final String CHANNEL_PREFIX = "progress:";

  private final StringRedisTemplate redis;

  public ProgressPublisher(StringRedisTemplate redis) {
    this.redis = redis;
  }

  public void publish(String runId, ProgressEvent event) {
    try {
      String json = MAPPER.writeValueAsString(event);
      redis.convertAndSend(CHANNEL_PREFIX + runId, json);
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize ProgressEvent for runId={}", runId, e);
    }
  }
}
