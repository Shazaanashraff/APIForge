package io.github.shazaanashraff.apiforge.modules.schemaparser;

/** Thrown when a spec cannot be parsed or fetched. */
public class SpecParseException extends RuntimeException {
  public SpecParseException(String message) {
    super(message);
  }

  public SpecParseException(String message, Throwable cause) {
    super(message, cause);
  }
}
