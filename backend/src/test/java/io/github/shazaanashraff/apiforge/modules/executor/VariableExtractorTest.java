package io.github.shazaanashraff.apiforge.modules.executor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VariableExtractorTest {

  @Test
  void extractsTopLevelField() {
    String json = "{\"id\":\"abc\"}";
    assertThat(VariableExtractor.extract("$.id", json)).isEqualTo("abc");
  }

  @Test
  void extractsNestedField() {
    String json = "{\"user\":{\"name\":\"Alice\"}}";
    assertThat(VariableExtractor.extract("$.user.name", json)).isEqualTo("Alice");
  }

  @Test
  void extractsArrayElement() {
    String json = "{\"items\":[{\"id\":\"x\"}]}";
    assertThat(VariableExtractor.extract("$.items[0].id", json)).isEqualTo("x");
  }

  @Test
  void returnsNullOnMissingPath() {
    assertThat(VariableExtractor.extract("$.missing", "{}")).isNull();
  }
}
