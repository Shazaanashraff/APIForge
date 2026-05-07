package io.github.shazaanashraff.apiforge.modules.executor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VariableStoreTest {

  @Test
  void putAndGetReturnsStoredValue() {
    VariableStore store = new VariableStore();
    store.put("name", "Alice");
    assertThat(store.get("name")).isEqualTo("Alice");
  }

  @Test
  void interpolateReplacesPlaceholder() {
    VariableStore store = new VariableStore();
    store.put("host", "localhost");
    assertThat(store.interpolate("http://${host}/api")).isEqualTo("http://localhost/api");
  }

  @Test
  void interpolateIgnoresMissingVariables() {
    VariableStore store = new VariableStore();
    assertThat(store.interpolate("${missing}")).isEqualTo("${missing}");
  }

  @Test
  void putOverwritesExistingValue() {
    VariableStore store = new VariableStore();
    store.put("k", "v1");
    store.put("k", "v2");
    assertThat(store.get("k")).isEqualTo("v2");
  }
}
