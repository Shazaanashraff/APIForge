package io.github.shazaanashraff.apiforge.modules.schemaparser;

import java.time.Instant;
import java.util.List;

/**
 * Result of a successful spec ingestion.
 *
 * @param endpoints     the normalised endpoint list
 * @param title         API title from the spec, or null
 * @param specVersion   OpenAPI version string ("3.0.x", "3.1.x") or "postman-2.1"
 * @param sourceUrl     the URL or filename the spec was loaded from; null for file uploads
 * @param parsedAt      when this parse result was produced
 * @param endpointCount total number of endpoints (convenience; equals endpoints.size())
 */
public record ParsedSpec(
    List<Endpoint> endpoints,
    String title,
    String specVersion,
    String sourceUrl,
    Instant parsedAt,
    int endpointCount) {

  public static ParsedSpec of(
      List<Endpoint> endpoints, String title, String specVersion, String sourceUrl) {
    return new ParsedSpec(
        endpoints, title, specVersion, sourceUrl, Instant.now(), endpoints.size());
  }
}
