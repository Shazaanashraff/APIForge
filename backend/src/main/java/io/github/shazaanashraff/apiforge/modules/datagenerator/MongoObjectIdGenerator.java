package io.github.shazaanashraff.apiforge.modules.datagenerator;

import java.util.Random;

/**
 * Generates MongoDB ObjectId strings in valid and intentionally invalid forms.
 *
 * <p>A valid MongoDB ObjectId is a 24-character lowercase hexadecimal string.
 * No MongoDB driver dependency is needed — we generate raw hex strings directly.
 */
public class MongoObjectIdGenerator {

  private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();
  private static final char[] NON_HEX_CHARS = "ghijklmnopqrstuvwxyz".toCharArray();

  private final Random random;

  public MongoObjectIdGenerator(Random random) {
    this.random = random;
  }

  /** Returns a valid ObjectId: exactly 24 lowercase hex characters. */
  public String validObjectId() {
    return randomHex(24);
  }

  /**
   * Returns a 24-character string that looks like an ObjectId but contains non-hex characters.
   * Tests that APIs correctly validate hex encoding.
   */
  public String invalidObjectId() {
    char[] result = randomHex(24).toCharArray();
    // Replace last 4 chars with non-hex
    for (int i = 20; i < 24; i++) {
      result[i] = NON_HEX_CHARS[random.nextInt(NON_HEX_CHARS.length)];
    }
    return new String(result);
  }

  /**
   * Returns a hex string of wrong length (23 or 25 chars).
   * Tests that APIs reject IDs of incorrect length.
   */
  public String wrongLengthObjectId() {
    // Alternate between too-short and too-long
    int length = random.nextBoolean() ? 23 : 25;
    return randomHex(length);
  }

  /**
   * Returns a standard UUID string — valid UUID format but not a 24-char hex ObjectId.
   * Tests that APIs reject UUIDs when ObjectIds are expected.
   */
  public String uuidLookingObjectId() {
    return java.util.UUID.nameUUIDFromBytes(
        Long.toHexString(random.nextLong()).getBytes()).toString();
  }

  private String randomHex(int length) {
    char[] buf = new char[length];
    for (int i = 0; i < length; i++) {
      buf[i] = HEX_CHARS[random.nextInt(HEX_CHARS.length)];
    }
    return new String(buf);
  }
}
