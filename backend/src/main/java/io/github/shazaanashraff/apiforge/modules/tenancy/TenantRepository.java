package io.github.shazaanashraff.apiforge.modules.tenancy;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Read-only access to tenants. Tenant creation is an admin-only operation (Phase 2). */
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

  Optional<Tenant> findBySlug(String slug);
}
