package io.github.shazaanashraff.apiforge.modules.schemaparser;

/**
 * Describes the ID format expected by path/query parameters named {@code id}, {@code *Id}, etc.
 *
 * <p>Lets the test generator produce correctly shaped IDs: UUID strings for REST APIs, 24-char hex
 * strings for MongoDB-backed APIs.
 *
 * @param format the detected format; {@code UNKNOWN} when no pattern could be inferred
 */
public record IdFormatHint(Format format) {

  public enum Format {
    UUID,
    OBJECTID,
    UNKNOWN
  }

  public static IdFormatHint unknown() {
    return new IdFormatHint(Format.UNKNOWN);
  }
}
