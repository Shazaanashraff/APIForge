package io.github.shazaanashraff.apiforge.modules.auth;

import io.github.shazaanashraff.apiforge.shared.BaseEntity;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Mirrors a Keycloak user locally so we can do JOINs and audit logs.
 *
 * <p>We don't store passwords here — authentication is handled entirely by Keycloak. The
 * keycloakId field is the JWT "sub" claim, which uniquely identifies the user in Keycloak.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity {

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  /** The Keycloak subject claim — matches the "sub" field in the JWT. */
  @Column(name = "keycloak_id", nullable = false, unique = true)
  private String keycloakId;

  @Column(name = "email", nullable = false)
  private String email;

  @Column(name = "name")
  private String name;

  /** Application-level role, sourced from the JWT "roles" claim. */
  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false, length = 50)
  private UserRole role = UserRole.USER;

  public enum UserRole {
    USER,
    ADMIN
  }
}
