package io.github.shazaanashraff.apiforge;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Verifies that Spring Modulith module boundaries are respected.
 *
 * <p>This test fails if any module imports from another module's internal package. Run it after
 * adding new cross-module code to ensure boundaries haven't been breached.
 */
class ApiForgeApplicationTest {

  @Test
  void moduleStructureIsValid() {
    // ApplicationModules.of() scans the package tree and verifies:
    // - No module reaches into another's internal packages
    // - No cycles in module dependencies
    ApplicationModules modules = ApplicationModules.of(ApiForgeApplication.class);
    modules.verify();
  }
}
