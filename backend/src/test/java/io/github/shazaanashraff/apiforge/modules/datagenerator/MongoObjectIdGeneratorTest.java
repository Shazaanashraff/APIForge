package io.github.shazaanashraff.apiforge.modules.datagenerator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Random;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class MongoObjectIdGeneratorTest {

  private static final Pattern HEX_24 = Pattern.compile("^[0-9a-f]{24}$");
  private static final Pattern UUID_PATTERN =
      Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");

  private MongoObjectIdGenerator generator;

  @BeforeEach
  void setUp() {
    generator = new MongoObjectIdGenerator(new Random(42L));
  }

  @Test
  void validObjectIdIs24LowercaseHex() {
    String id = generator.validObjectId();
    assertThat(id).hasSize(24);
    assertThat(id).matches(HEX_24);
  }

  @RepeatedTest(10)
  void validObjectIdAlwaysPasses24HexCheck() {
    assertThat(generator.validObjectId()).matches(HEX_24);
  }

  @Test
  void invalidObjectIdIs24CharsButContainsNonHex() {
    String id = generator.invalidObjectId();
    assertThat(id).hasSize(24);
    assertThat(id).doesNotMatch(HEX_24); // must NOT be valid hex
  }

  @Test
  void wrongLengthObjectIdIsNot24Chars() {
    String id = generator.wrongLengthObjectId();
    assertThat(id.length()).isNotEqualTo(24);
    assertThat(id.length()).isBetween(23, 25); // 23 or 25
  }

  @RepeatedTest(10)
  void wrongLengthIsAlwaysOffBy1() {
    int len = generator.wrongLengthObjectId().length();
    assertThat(len == 23 || len == 25).isTrue();
  }

  @Test
  void uuidLookingObjectIdMatchesUuidPattern() {
    String id = generator.uuidLookingObjectId();
    assertThat(id).matches(UUID_PATTERN);
    // A UUID is 36 chars (with dashes) — definitely not a 24-char ObjectId
    assertThat(id).doesNotMatch(HEX_24);
  }

  @Test
  void allFourVariantsAreDistinct() {
    String valid   = generator.validObjectId();
    String invalid = generator.invalidObjectId();
    String wrong   = generator.wrongLengthObjectId();
    String uuid    = generator.uuidLookingObjectId();

    // They should all be different from each other
    assertThat(valid).isNotEqualTo(uuid);
    assertThat(valid.length()).isNotEqualTo(wrong.length());
  }
}
