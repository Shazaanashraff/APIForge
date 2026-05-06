package io.github.shazaanashraff.apiforge;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Verifies Spring Modulith module boundaries — no inter-module internal package access, no cycles.
 *
 * <p>This is a fast static analysis test (no Spring context loaded). Run after adding new
 * cross-module code to confirm boundaries are respected.
 */
class ApiForgeApplicationTest {

  @Test
  void moduleStructureIsValid() {
    ApplicationModules modules = ApplicationModules.of(ApiForgeApplication.class);
    modules.verify();
  }
}
