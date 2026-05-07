package io.github.shazaanashraff.apiforge.modules.sse;

public record ProgressEvent(String runId, ProgressEventType type, String payload, long timestamp) {

  public static ProgressEvent of(String runId, ProgressEventType type, String payload) {
    return new ProgressEvent(runId, type, payload, System.currentTimeMillis());
  }
}
