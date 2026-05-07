package io.github.shazaanashraff.apiforge.modules.schemaparser;

/**
 * Estimated maximum request body size, derived from schema {@code maxLength} / {@code maxItems}.
 *
 * <p>Used by the test generator to craft oversized-payload tests: it sends {@code declaredMaxBytes
 * * 10} bytes to verify the server rejects the request.
 *
 * @param declaredMaxBytes best-effort upper bound in bytes; -1 means no limit declared
 * @param hasDeclaredMax true when the spec explicitly constrains the size
 */
public record PayloadSizeHint(long declaredMaxBytes, boolean hasDeclaredMax) {

  public static PayloadSizeHint unknown() {
    return new PayloadSizeHint(-1, false);
  }
}
