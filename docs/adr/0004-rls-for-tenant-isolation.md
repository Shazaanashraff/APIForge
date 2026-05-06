# ADR-0004: PostgreSQL Row-Level Security for Tenant Isolation

**Date:** 2026-05-06
**Status:** Accepted
**Section:** S03

## Context

APIForge is a multi-tenant SaaS tool. All tenant data (projects, test runs, results) lives in a
shared Postgres schema. We need a mechanism that guarantees tenant A can never read or mutate
tenant B's rows — even if a bug in the application layer accidentally omits a `WHERE tenant_id = ?`
filter.

Options evaluated:

1. **Application-only filtering**: every query explicitly includes a `tenant_id` predicate.
2. **Separate schema per tenant**: each tenant gets its own Postgres schema; the connection
   string determines which schema is active.
3. **Row-Level Security (RLS) as the enforcement layer**, with application-level filtering as an
   additional guard.

## Decision

Use **PostgreSQL Row-Level Security** (option 3) as the primary enforcement mechanism:

- RLS policies on every tenant-scoped table check the session variable
  `app.current_tenant_id` (set via `SET LOCAL` inside each transaction).
- A dedicated low-privilege role (`apiforge_app`) is subject to RLS;
  the Flyway superuser bypasses it (standard Postgres pattern).
- `TenantContextHolder` (ThreadLocal) carries the tenant UUID from the inbound JWT to the
  repository layer.
- `TenantAwareQueryInterceptor` (AOP `@Before`) reads from the holder and executes
  `SET LOCAL app.current_tenant_id = '<uuid>'` inside each active Spring transaction.
- Application-level `WHERE tenant_id = ?` predicates remain as a redundant safety net.

## Alternatives Considered

- **Application-only filtering:** Requires every developer to remember to add the predicate.
  One missed `findAll()` call leaks cross-tenant data. Not suitable for a security boundary.
- **Separate schema per tenant:** Eliminates cross-tenant risk at the DB level but makes
  Flyway migrations, connection pooling, and reporting far more complex. Ruled out because
  we want to onboard tenants without schema provisioning overhead.

## Consequences

**Positive:**
- Defense-in-depth: even a buggy query with no tenant filter returns zero rows for the wrong tenant.
- Proven Postgres pattern — RLS is the standard multi-tenancy approach used by production SaaS products.
- The `apiforge_app` role (used by the running app) cannot bypass RLS even via raw JDBC.
- Easy to audit: the full policy is in one SQL migration file (V3).

**Negative:**
- `SET LOCAL` only works inside a transaction, so all repository calls must originate
  from a `@Transactional` service method. Calling repositories directly in tests without
  a wrapping transaction skips RLS enforcement.
- Adding a `SET LOCAL` statement per transaction adds one extra round-trip to every
  database operation. Overhead is negligible (<1 ms) but non-zero.

**Neutral:**
- The Flyway migration role (Postgres superuser) bypasses RLS by design; admin operations
  run as the superuser and are not subject to per-tenant filtering.
- FORCE ROW LEVEL SECURITY is used only in the integration test migration to ensure the
  test user is also subject to the policy.
