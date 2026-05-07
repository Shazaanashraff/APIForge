package io.github.shazaanashraff.apiforge.modules.testgenerator;

/** A single expectation the executor verifies against the HTTP response. */
public record Assertion(AssertionType type, String key, String value) {

  public enum AssertionType {
    STATUS_CODE,
    STATUS_CODE_RANGE,
    RESPONSE_SCHEMA,
    HEADER_PRESENT,
    HEADER_VALUE,
    RESPONSE_TIME_MS
  }

  public static Assertion statusCode(int code) {
    return new Assertion(AssertionType.STATUS_CODE, "status", String.valueOf(code));
  }

  public static Assertion statusCodeRange(String range) {
    return new Assertion(AssertionType.STATUS_CODE_RANGE, "status", range);
  }

  public static Assertion responseSchema() {
    return new Assertion(AssertionType.RESPONSE_SCHEMA, "body", null);
  }

  public static Assertion responseTimeBelow(long thresholdMs) {
    return new Assertion(
        AssertionType.RESPONSE_TIME_MS, "responseTimeMs", String.valueOf(thresholdMs));
  }

  public static Assertion headerPresent(String header) {
    return new Assertion(AssertionType.HEADER_PRESENT, header, null);
  }
}
