package io.github.shazaanashraff.apiforge.modules.schemaparser;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IdFormatDetectorTest {

  private IdFormatDetector detector;

  @BeforeEach
  void setUp() {
    detector = new IdFormatDetector();
  }

  // ── detect(schemaFormat, schemaPattern) ──────────────────────────────────

  @Test
  void detectsUuidFromFormat() {
    assertThat(detector.detect("uuid", null).format()).isEqualTo(IdFormatHint.Format.UUID);
  }

  @Test
  void detectsUuidFromFormatCaseInsensitive() {
    assertThat(detector.detect("UUID", null).format()).isEqualTo(IdFormatHint.Format.UUID);
  }

  @Test
  void detectsObjectIdFromFormat() {
    assertThat(detector.detect("objectid", null).format()).isEqualTo(IdFormatHint.Format.OBJECTID);
  }

  @Test
  void detectsObjectIdFromFormatCaseInsensitive() {
    assertThat(detector.detect("ObjectId", null).format()).isEqualTo(IdFormatHint.Format.OBJECTID);
  }

  @Test
  void detectsObjectIdFromPattern() {
    assertThat(detector.detect(null, "^[0-9a-fA-F]{24}$").format())
        .isEqualTo(IdFormatHint.Format.OBJECTID);
  }

  @Test
  void returnsUnknownForNullInputs() {
    assertThat(detector.detect(null, null).format()).isEqualTo(IdFormatHint.Format.UNKNOWN);
  }

  @Test
  void returnsUnknownForUnrecognisedFormat() {
    assertThat(detector.detect("int64", null).format()).isEqualTo(IdFormatHint.Format.UNKNOWN);
  }

  // ── detectFromValue ──────────────────────────────────────────────────────

  @Test
  void detectsUuidFromValue() {
    assertThat(detector.detectFromValue("550e8400-e29b-41d4-a716-446655440000").format())
        .isEqualTo(IdFormatHint.Format.UUID);
  }

  @Test
  void detectsObjectIdFromValue() {
    assertThat(detector.detectFromValue("507f1f77bcf86cd799439011").format())
        .isEqualTo(IdFormatHint.Format.OBJECTID);
  }

  @Test
  void returnsUnknownForArbitraryString() {
    assertThat(detector.detectFromValue("john-doe").format()).isEqualTo(IdFormatHint.Format.UNKNOWN);
  }

  @Test
  void returnsUnknownForNullValue() {
    assertThat(detector.detectFromValue(null).format()).isEqualTo(IdFormatHint.Format.UNKNOWN);
  }

  @Test
  void shortHexIsNotObjectId() {
    // ObjectId is exactly 24 hex chars — 12 is too short
    assertThat(detector.detectFromValue("507f1f77bcf8").format()).isEqualTo(IdFormatHint.Format.UNKNOWN);
  }
}
