package io.github.shazaanashraff.apiforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulith;

/**
 * APIForge — entry point.
 *
 * <p>The @Modulith annotation tells Spring Modulith to enforce module boundaries. It verifies at
 * startup (and in tests) that modules only communicate through their public API or via application
 * events — never by reaching into each other's internal packages.
 *
 * <p>Modules live under: io.github.shazaanashraff.apiforge.modules.*
 */
@SpringBootApplication
@Modulith(systemName = "APIForge")
public class ApiForgeApplication {

  public static void main(String[] args) {
    SpringApplication.run(ApiForgeApplication.class, args);
  }
}
