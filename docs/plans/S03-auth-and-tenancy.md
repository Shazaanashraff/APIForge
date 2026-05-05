# S03 — Auth & Multi-Tenancy

**Status:** ⬜ Pending
**Complexity:** L
**Depends on:** S02

## Goal

Secure the API with JWT validation against Keycloak-issued tokens. Propagate the tenant ID from the JWT into a thread-local context. Use that context to set a Postgres session variable so RLS policies can filter rows automatically.

## Architecture Decisions

- **Keycloak as IdP:** runs in Docker Compose, realm auto-imported on first start
- **Spring Security resource server:** validates JWT signature against Keycloak's JWKS endpoint
- **`TenantContextHolder`** (thread-local): stores `tenantId` extracted from JWT claims
- **`TenantAwareDataSource`** / Hibernate interceptor: sets `SET LOCAL app.current_tenant_id = '...'` before each DB operation
- **Phase 1 default:** single seeded tenant (`demo-tenant`) works without multi-tenant onboarding
- **Bearer JWT + API key + Basic auth schemes** for generated tests (MVP scope)

## Step-by-Step Tasks

### CP1 — Keycloak realm setup
- [ ] Create `observability/keycloak/realm-export.json`:
  - Realm: `apiforge`
  - Client: `apiforge-backend` (confidential, for resource server)
  - Client: `apiforge-frontend` (public, PKCE)
  - Demo user: `demo@apiforge.dev` / `demo123` with `ROLE_USER`
  - Realm role: `ROLE_USER`, `ROLE_ADMIN`
  - Custom mapper: adds `tenantId` claim to JWT

### CP2 — Spring Security config
- [ ] `SecurityConfig.java` in `modules.auth`:
  - `@EnableWebSecurity`
  - JWT validation via `spring.security.oauth2.resourceserver.jwt.issuer-uri`
  - Permit: `/actuator/health`, `/v3/api-docs`, `/swagger-ui/**`
  - Require authenticated: everything else
  - Extract `tenantId` from JWT → set in `TenantContextHolder`

### CP3 — Tenant context propagation
- [ ] `TenantContextHolder.java` in `modules.tenancy`:
  - `ThreadLocal<UUID> tenantId`
  - `set(UUID)` / `get()` / `clear()` — always clear in a `finally` block
- [ ] `TenantContextFilter.java` — `OncePerRequestFilter` that extracts tenantId from JWT and sets it
- [ ] `TenantAwareQueryInterceptor.java` — Hibernate `StatementInspector` that prepends `SET LOCAL app.current_tenant_id = '...'` before queries

### CP4 — Integration test for tenant isolation
- [ ] `TenantIsolationIntegrationTest`:
  - Create project for tenant A
  - Query as tenant B → expect empty result
  - Verify RLS is enforced at DB level

### CP5 — Actuator security
- [ ] Secure `/actuator` endpoints: only allow from localhost or with `ROLE_ADMIN`

## Files to Create/Modify

```
observability/keycloak/realm-export.json
backend/src/main/java/.../modules/auth/
  SecurityConfig.java
  JwtTenantExtractor.java
backend/src/main/java/.../modules/tenancy/
  TenantContextHolder.java
  TenantContextFilter.java
  TenantAwareQueryInterceptor.java
backend/src/test/java/.../modules/tenancy/
  TenantIsolationIntegrationTest.java
```

## Manual Verification

```powershell
# Get a token from Keycloak
$token = (Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/realms/apiforge/protocol/openid-connect/token" `
  -Body "grant_type=password&client_id=apiforge-backend&username=demo@apiforge.dev&password=demo123&scope=openid" `
  -ContentType "application/x-www-form-urlencoded").access_token

# Request without token → 401
Invoke-WebRequest "http://localhost:8081/api/projects" -UseBasicParsing
# Expected: 401

# Request with token → 200
Invoke-WebRequest "http://localhost:8081/api/projects" -Headers @{Authorization="Bearer $token"} -UseBasicParsing
# Expected: 200 with empty array
```

## Definition of Done

- [ ] Unauthenticated request → 401
- [ ] Valid JWT → request passes, tenantId in context
- [ ] `TenantIsolationIntegrationTest` passes
- [ ] ADR: `0005-keycloak-as-identity-provider.md`
- [ ] PROGRESS.md updated
