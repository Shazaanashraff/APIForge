package io.github.shazaanashraff.apiforge.modules.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.shazaanashraff.apiforge.TestSecurityConfig;
import io.github.shazaanashraff.apiforge.modules.tenancy.Tenant;
import io.github.shazaanashraff.apiforge.modules.tenancy.TenantRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Integration test for ProjectService.
 *
 * <p>Uses Testcontainers to spin up a real PostgreSQL instance. This runs all Flyway migrations
 * against a fresh DB, ensuring the schema is correct and the service layer works end-to-end.
 *
 * <p>NOTE: We use the standard postgres image here (not timescale) because TimescaleDB availability
 * in CI isn't guaranteed. The V2 migration is annotated to be skipped in the 'test' Spring profile.
 * The core schema (V1) and RLS (V3) still run.
 *
 * <p>Run with: mvnw verify -Pintegration
 */
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class ProjectServiceIntegrationTest {

  // Testcontainers starts a fresh Postgres container for this test class.
  // @Container on a static field means the container is shared across all tests in this class.
  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
          .withDatabaseName("apiforge_test")
          .withUsername("apiforge")
          .withPassword("apiforge_test_secret");

  // Override Spring datasource config to point at the Testcontainers Postgres.
  @DynamicPropertySource
  static void configureDataSource(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired private ProjectService projectService;

  @Autowired private TenantRepository tenantRepository;

  private UUID tenantAId;
  private UUID tenantBId;

  @BeforeEach
  void setUp() {
    // Create two tenants for isolation testing
    Tenant tenantA = tenantRepository.save(new Tenant("Tenant A", "tenant-a"));
    Tenant tenantB = tenantRepository.save(new Tenant("Tenant B", "tenant-b"));
    tenantAId = tenantA.getId();
    tenantBId = tenantB.getId();
  }

  @Test
  void createAndRetrieveProject() {
    Project created =
        projectService.createProject(tenantAId, "My API", "http://localhost:8090", null);

    assertThat(created.getId()).isNotNull();
    assertThat(created.getName()).isEqualTo("My API");
    assertThat(created.getBaseUrl()).isEqualTo("http://localhost:8090");
    assertThat(created.getTenantId()).isEqualTo(tenantAId);
    assertThat(created.isMongoBackedApi()).isFalse();

    Project fetched = projectService.getProject(created.getId());
    assertThat(fetched.getId()).isEqualTo(created.getId());
  }

  @Test
  void listProjectsOnlyReturnsTenantOwnedProjects() {
    // Create one project per tenant
    projectService.createProject(tenantAId, "Tenant A Project", "http://a.example.com", null);
    projectService.createProject(tenantBId, "Tenant B Project", "http://b.example.com", null);

    List<Project> tenantAProjects = projectService.listProjects(tenantAId);
    List<Project> tenantBProjects = projectService.listProjects(tenantBId);

    // Each tenant sees only their own projects
    assertThat(tenantAProjects).hasSize(1);
    assertThat(tenantAProjects.get(0).getName()).isEqualTo("Tenant A Project");

    assertThat(tenantBProjects).hasSize(1);
    assertThat(tenantBProjects.get(0).getName()).isEqualTo("Tenant B Project");
  }

  @Test
  void getProjectThrowsNotFoundForUnknownId() {
    assertThatThrownBy(() -> projectService.getProject(UUID.randomUUID()))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("not found");
  }

  @Test
  void updateProjectPersistsChanges() {
    Project project =
        projectService.createProject(tenantAId, "Original Name", "http://old.example.com", null);

    Project updated =
        projectService.updateProject(
            project.getId(), "Updated Name", "http://new.example.com", true);

    assertThat(updated.getName()).isEqualTo("Updated Name");
    assertThat(updated.getBaseUrl()).isEqualTo("http://new.example.com");
    assertThat(updated.isMongoBackedApi()).isTrue();
  }

  @Test
  void deleteProjectRemovesIt() {
    Project project = projectService.createProject(tenantAId, "To Delete", null, null);
    projectService.deleteProject(project.getId());

    assertThatThrownBy(() -> projectService.getProject(project.getId()))
        .isInstanceOf(ResponseStatusException.class);
  }
}
