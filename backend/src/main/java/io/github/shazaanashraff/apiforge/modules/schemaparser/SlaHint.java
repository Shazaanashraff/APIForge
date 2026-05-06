package io.github.shazaanashraff.apiforge.modules.schemaparser;

/**
 * Response-time SLA in milliseconds, sourced from the {@code x-response-time-sla} vendor extension.
 *
 * <p>When present, the test executor uses this threshold to assert that the actual response time
 * is below the declared SLA. Falls back to the global config value when absent.
 *
 * @param thresholdMs declared SLA in ms; {@code null} means no SLA declared in the spec
 */
public record SlaHint(Long thresholdMs) {

  public static SlaHint none() {
    return new SlaHint(null);
  }

  public boolean hasSla() {
    return thresholdMs != null;
  }
}
