package io.github.shazaanashraff.apiforge.modules.project;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

  /** List all projects for a tenant, newest first. RLS enforces tenant isolation. */
  List<Project> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);
}
