package io.github.shazaanashraff.apiforge.modules.auth;

import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Extracts the {@code tenantId} claim from a validated Keycloak JWT.
 *
 * <p>The claim is injected by the realm-level protocol mapper defined in {@code
 * keycloak/realm-export.json}. Its value is the UUID of the tenant that owns the authenticated
 * user.
 *
 * <p>Returns {@code null} if the claim is absent or not a valid UUID — callers must handle that.
 */
@Component
public class JwtTenantExtractor {

  private static final String TENANT_ID_CLAIM = "tenantId";

  public UUID extract(Jwt jwt) {
    String raw = jwt.getClaimAsString(TENANT_ID_CLAIM);
    if (raw == null || raw.isBlank()) {
      return null;
    }
    try {
      return UUID.fromString(raw);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
