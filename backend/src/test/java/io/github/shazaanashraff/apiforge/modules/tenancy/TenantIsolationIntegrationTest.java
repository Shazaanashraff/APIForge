package io.github.shazaanashraff.apiforge.modules.tenancy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.shazaanashraff.apiforge.TestSecurityConfig;
import io.github.shazaanashraff.apiforge.modules.project.Project;
import io.github.shazaanashraff.apiforge.modules.project.ProjectService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Proves that Postgres Row-Level Security (RLS) enforces tenant isolation at the database layer.
 *
 * <p>This test uses a separate Flyway migration location ({@code isolation-test}) that adds a
 * FORCE RLS policy on the {@code projects} table on top of the standard test schema (V1). FORCE
 * means even the table-owner role used by Testcontainers is subject to the policy — the same
 * behaviour as production where the {@code apiforge_app} role is restricted.
 *
 * <p>The full path exercised:
 * <ol>
 *   <li>{@code TenantContextHolder.set(tenantId)} — simulates what TenantContextFilter does after
 *       reading the JWT claim in a real HTTP request.
 *   <li>Service method ({@code @Transactional}) starts a transaction.
 *   <li>{@code TenantAwareQueryInterceptor} fires on the repository call (inside the transaction)
 *       and executes {@code SET LOCAL app.current_tenant_id = '<uuid>'}.
 *   <li>Postgres RLS evaluates {@code current_setting('app.current_tenant_id', TRUE)::uuid} and
 *       filters {@code SELECT} results to the matching tenant.
 * </ol>
 *
 * <p>Run with: {@code mvnw verify -Pintegration}
 */
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class TenantIsolationIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
          .withDatabaseName("apiforge_isolation_test")
          .withUsername("apiforge")
          .withPassword("apiforge_test_secret");

  /**
   * Override Flyway locations to include the isolation-test migration (V2__rls_force.sql)
   * in addition to the standard test schema (V1__create_core_schema.sql).
   */
  @DynamicPropertySource
  static void configureDataSource(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add(
        "spring.flyway.locations",
        () -> "classpath:db/migration/test,classpath:db/migration/isolation-test");
  }

  @Autowired private ProjectService projectService;
  @Autowired private TenantRepository tenantRepository;

  private Tenant tenantA;
  private Tenant tenantB;

  @BeforeEach
  void setUp() {
    TenantContextHolder.clear();
    tenantA = tenantRepository.save(new Tenant("Isolation-Tenant-A", "iso-tenant-a"));
    tenantB = tenantRepository.save(new Tenant("Isolation-Tenant-B", "iso-tenant-b"));
  }

  @AfterEach
  void tearDown() {
    TenantContextHolder.clear();
  }

  /**
   * Core RLS test: a project created under tenant A must not be visible when querying as tenant B.
   *
   * <p>The RLS policy hides the row at the database level — even though the application code uses
   * {@code findById} (no explicit tenant filter in the query), the row simply does not exist from
   * tenant B's perspective. The service translates the empty result into a 404.
   */
  @Test
  void projectIsHiddenFromOtherTenantByRls() {
    // --- ARRANGE: create a project as tenant A -----------------------------------------
    TenantContextHolder.set(tenantA.getId());
    Project project =
        projectService.createProject(tenantA.getId(), "Secret Project", "http://a.example.com", null);
    assertThat(project.getId()).isNotNull();

    // --- ACT / ASSERT: tenant A can see their own project ------------------------------
    Project found = projectService.getProject(project.getId());
    assertThat(found.getId()).isEqualTo(project.getId());
    assertThat(found.getName()).isEqualTo("Secret Project");

    // --- ACT / ASSERT: tenant B's context hides tenant A's project via RLS -------------
    // TenantAwareQueryInterceptor will SET LOCAL app.current_tenant_id = tenantB.
    // The RLS policy on 'projects' filters out rows where tenant_id != tenantB → empty.
    // ProjectService.getProject() turns the empty Optional into a 404 ResponseStatusException.
    TenantContextHolder.set(tenantB.getId());
    assertThatThrownBy(() -> projectService.getProject(project.getId()))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            });
  }

  /** Sanity check: each tenant sees only their own projects via RLS-filtered list. */
  @Test
  void listProjectsReturnsOnlyTenantOwnedRowsUnderRls() {
    TenantContextHolder.set(tenantA.getId());
    projectService.createProject(tenantA.getId(), "A-Project", null, null);

    TenantContextHolder.set(tenantB.getId());
    projectService.createProject(tenantB.getId(), "B-Project", null, null);

    // listProjects uses findByTenantIdOrderByCreatedAtDesc — the application-level filter
    // combined with RLS both enforce isolation; here we verify the combined behaviour.
    TenantContextHolder.set(tenantA.getId());
    assertThat(projectService.listProjects(tenantA.getId()))
        .hasSize(1)
        .allMatch(p -> p.getName().equals("A-Project"));

    TenantContextHolder.set(tenantB.getId());
    assertThat(projectService.listProjects(tenantB.getId()))
        .hasSize(1)
        .allMatch(p -> p.getName().equals("B-Project"));
  }
}
