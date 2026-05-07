package io.github.shazaanashraff.apiforge.modules.schemaparser;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Infers the ID format (UUID vs MongoDB ObjectId) from a schema's declared format and pattern.
 *
 * <p>Detection rules (first match wins):
 *
 * <ol>
 *   <li>{@code format: uuid} → UUID
 *   <li>{@code format: objectid} (vendor extension) → OBJECTID
 *   <li>Schema pattern matches 24-char hex regex → OBJECTID
 *   <li>Otherwise → UNKNOWN
 * </ol>
 */
@Component
public class IdFormatDetector {

  private static final Pattern OBJECTID_PATTERN =
      Pattern.compile("^\\^?\\[0-9a-fA-F\\]\\{24\\}\\$?$");
  private static final Pattern OBJECTID_VALUE_PATTERN = Pattern.compile("^[0-9a-fA-F]{24}$");

  /**
   * Detects format from explicit format and pattern strings sourced from a JSON Schema.
   *
   * @param schemaFormat the {@code format} field value (e.g. "uuid", "objectid"); may be null
   * @param schemaPattern the {@code pattern} regex value; may be null
   */
  public IdFormatHint detect(String schemaFormat, String schemaPattern) {
    if ("uuid".equalsIgnoreCase(schemaFormat)) {
      return new IdFormatHint(IdFormatHint.Format.UUID);
    }
    if ("objectid".equalsIgnoreCase(schemaFormat)) {
      return new IdFormatHint(IdFormatHint.Format.OBJECTID);
    }
    if (schemaPattern != null && OBJECTID_PATTERN.matcher(schemaPattern).matches()) {
      return new IdFormatHint(IdFormatHint.Format.OBJECTID);
    }
    return IdFormatHint.unknown();
  }

  /**
   * Detects format from a concrete value string (e.g. an example or default value). Useful for
   * Postman collections where only example values are available, not schemas.
   */
  public IdFormatHint detectFromValue(String value) {
    if (value == null) {
      return IdFormatHint.unknown();
    }
    if (OBJECTID_VALUE_PATTERN.matcher(value).matches()) {
      return new IdFormatHint(IdFormatHint.Format.OBJECTID);
    }
    try {
      java.util.UUID.fromString(value);
      return new IdFormatHint(IdFormatHint.Format.UUID);
    } catch (IllegalArgumentException e) {
      return IdFormatHint.unknown();
    }
  }
}
