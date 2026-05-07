package io.github.shazaanashraff.apiforge.modules.tenancy;

import io.github.shazaanashraff.apiforge.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents an organisation (tenant) that owns projects.
 *
 * <p>In Phase 1 there is a single seeded demo tenant. Multi-tenant onboarding is a Phase 2 concern;
 * for now, all requests default to this demo tenant.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tenants")
public class Tenant extends BaseEntity {

  @Column(name = "name", nullable = false)
  private String name;

  /** URL-safe identifier, e.g. "demo" or "acme-corp". Used in routes and logs. */
  @Column(name = "slug", nullable = false, unique = true, length = 100)
  private String slug;

  public Tenant(String name, String slug) {
    this.name = name;
    this.slug = slug;
  }
}
