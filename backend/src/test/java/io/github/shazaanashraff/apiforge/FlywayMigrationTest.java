package io.github.shazaanashraff.apiforge;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Verifies that all Flyway migrations run in order on a clean database. Runs against plain Postgres
 * (no TimescaleDB) — uses the test migration set.
 */
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class FlywayMigrationTest {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
          .withDatabaseName("flyway_test")
          .withUsername("apiforge")
          .withPassword("test_secret");

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired private Flyway flyway;

  @Test
  void allMigrationsApplySuccessfully() {
    var info = flyway.info();
    // All applied migrations should have SUCCESS status
    long failedMigrations =
        java.util.Arrays.stream(info.applied()).filter(m -> m.getState().isFailed()).count();
    assertThat(failedMigrations).as("No Flyway migrations should have failed").isZero();

    // At least V1 must exist
    assertThat(info.applied()).hasSizeGreaterThanOrEqualTo(1);
  }
}
