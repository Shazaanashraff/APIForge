package io.github.shazaanashraff.apiforge.modules.project;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiSpecRepository extends JpaRepository<ApiSpec, UUID> {

  /** Returns the most recently created spec for a project. */
  Optional<ApiSpec> findTopByProjectIdOrderByCreatedAtDesc(UUID projectId);
}
