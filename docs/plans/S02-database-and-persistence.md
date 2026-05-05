# S02 — Database & Persistence

**Status:** ⬜ Pending
**Complexity:** M
**Depends on:** S01

## Goal

Set up the complete database layer: PostgreSQL 16 + TimescaleDB, Flyway migrations for the full schema, JPA entities and Spring Data repositories for all domain objects, and row-level security (RLS) policies scaffolded for multi-tenancy.

## Architecture Decisions

- **TimescaleDB** for load metric hypertable — transparent to JPA
- **Flyway** manages all schema changes — never modify existing migrations
- **RLS** policies created now, enforced in S03 (requires `SET LOCAL` with tenant ID)
- **HikariCP** for connection pooling (Spring Boot default)
- **`@Transactional`** on all service methods that write to the database

## Step-by-Step Tasks

### CP1 — Flyway migrations (core schema)
- [ ] Create `backend/src/main/resources/db/migration/V1__create_core_schema.sql`
  - Tables: `tenants`, `users`, `projects`, `api_specs`, `test_runs`, `test_cases`, `test_results`
  - All tenant-scoped tables have: `tenant_id UUID NOT NULL`, `created_at TIMESTAMPTZ`, `updated_at TIMESTAMPTZ`
  - Indexes on all foreign keys and common query columns
- [ ] Create `V2__enable_timescaledb.sql`
  - `CREATE EXTENSION IF NOT EXISTS timescaledb CASCADE;`
  - `SELECT create_hypertable('load_metrics', 'sampled_at');`
- [ ] Create `V3__rls_policies.sql`
  - Enable RLS on all tenant-scoped tables
  - Create permissive policy: `USING (tenant_id = current_setting('app.current_tenant_id')::uuid)`
  - Create a DB role `apiforge_app` that the app uses (not superuser)

### CP2 — JPA entities
- [ ] `Tenant.java` — `@Entity` in `modules.tenancy`
- [ ] `User.java` — linked to Tenant
- [ ] `Project.java` — `tenantId`, `name`, `baseUrl`, `isMongoBackedApi` flag, `specType`
- [ ] `ApiSpec.java` — `projectId`, `specContent` (TEXT), `parsedAt`
- [ ] `TestRun.java` — `projectId`, `status` (PENDING/RUNNING/COMPLETED/FAILED), `startedAt`, `completedAt`
- [ ] `TestCase.java` — `testRunId`, `category` (enum of 11), `endpointPath`, `httpMethod`, input, expected
- [ ] `TestResult.java` — `testCaseId`, `actualStatusCode`, `responseTimeMs`, `passed`, `failureReason`
- [ ] `LoadMetric.java` — `testRunId`, `sampledAt`, `rps`, `p50`, `p95`, `p99`, `errorRate`

### CP3 — Repositories + services
- [ ] `ProjectRepository extends JpaRepository<Project, UUID>` in `modules.project`
- [ ] `TestRunRepository` with query: find by projectId + status
- [ ] `TestResultRepository` with query: find by testRunId, group by category
- [ ] `LoadMetricRepository` with TimescaleDB time-bucket query
- [ ] `ProjectService` — CRUD with tenant context
- [ ] Integration test with Testcontainers `@PostgreSQLContainer`

### CP4 — DB init scripts for Docker
- [ ] `scripts/db-init/01_create_sample_java_db.sql` — creates `sample_java_db` DB and user for Java sample API

## Files to Create/Modify

```
backend/src/main/resources/db/migration/
  V1__create_core_schema.sql
  V2__enable_timescaledb.sql
  V3__rls_policies.sql
backend/src/main/java/.../modules/tenancy/
  Tenant.java
backend/src/main/java/.../modules/project/
  Project.java  ApiSpec.java  ProjectRepository.java  ProjectService.java
backend/src/main/java/.../modules/executor/
  TestRun.java  TestCase.java  TestResult.java
backend/src/main/java/.../modules/loadtester/
  LoadMetric.java  LoadMetricRepository.java
backend/src/test/java/.../modules/project/
  ProjectServiceIntegrationTest.java
scripts/db-init/
  01_create_sample_java_db.sql
```

## Tests to Write

- `ProjectServiceIntegrationTest` — create project, read back, verify tenant isolation
- `FlywayMigrationTest` — verify all migrations run in order on a clean DB

## Manual Verification

```powershell
# After backend starts:
docker exec apiforge-postgres psql -U apiforge -d apiforge_db -c "\dt"
# Expected: tables listed (tenants, users, projects, ...)
.\mvnw.cmd flyway:info
# Expected: all migrations show "Success"
```

## Definition of Done

- [ ] All migrations run cleanly from scratch
- [ ] `ProjectServiceIntegrationTest` passes with Testcontainers
- [ ] `.\mvnw.cmd test` is green
- [ ] ADR written: `0004-rls-for-tenant-isolation.md`
- [ ] PROGRESS.md updated
