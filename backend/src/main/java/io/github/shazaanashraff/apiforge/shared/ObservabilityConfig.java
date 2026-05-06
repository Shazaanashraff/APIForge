package io.github.shazaanashraff.apiforge.shared;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Global Micrometer configuration.
 *
 * <p>Registers common tags on every metric so Prometheus queries and Grafana dashboards can
 * filter by application name and deployment environment without adding those dimensions to
 * every individual metric definition.
 *
 * <p>{@code application} tag: already set by {@code management.metrics.tags.application} in
 * application.yml (Spring Boot default). We add {@code environment} here as a supplementary tag.
 */
@Configuration
public class ObservabilityConfig {

  /**
   * Attaches a global {@code environment} tag (e.g. "local", "staging", "prod") to every metric.
   * Override via the {@code APIFORGE_ENVIRONMENT} environment variable in Docker Compose or CI.
   */
  @Bean
  public MeterRegistryCustomizer<MeterRegistry> commonTags(
      @Value("${apiforge.environment:local}") String environment) {
    return registry -> registry.config().commonTags("environment", environment);
  }
}
