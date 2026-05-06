-- ─────────────────────────────────────────────────────────────────────────────
-- V3: Row-Level Security (RLS) policies for multi-tenancy
--
-- HOW THIS WORKS:
-- 1. The application sets a Postgres session variable before each query:
--       SET LOCAL app.current_tenant_id = '<uuid>';
-- 2. The RLS policy on each table checks this variable.
-- 3. Even if application code forgets to filter by tenantId, the DB enforces it.
--
-- PHASE 1 NOTE: We scaffold the policies now but they won't actively block
-- anything until S03 wires up TenantContextHolder to set the session variable.
-- Until then, the demo tenant seed data is accessible to all connections.
-- ─────────────────────────────────────────────────────────────────────────────

-- Create a dedicated application role (not superuser) for the Spring app.
-- The superuser can always bypass RLS; the app role cannot.
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'apiforge_app') THEN
        CREATE ROLE apiforge_app LOGIN PASSWORD 'apiforge_dev_secret';
    END IF;
END
$$;

-- Grant the app role access to the database and all tables/sequences
GRANT CONNECT ON DATABASE apiforge_db TO apiforge_app;
GRANT USAGE ON SCHEMA public TO apiforge_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO apiforge_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO apiforge_app;
-- Future tables will also be accessible
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO apiforge_app;

-- ── Enable RLS on every tenant-scoped table ────────────────────────────────────
-- Note: tenants itself is NOT tenant-scoped (it's the root table).
ALTER TABLE users         ENABLE ROW LEVEL SECURITY;
ALTER TABLE projects      ENABLE ROW LEVEL SECURITY;
ALTER TABLE api_specs     ENABLE ROW LEVEL SECURITY;
ALTER TABLE test_runs     ENABLE ROW LEVEL SECURITY;
ALTER TABLE test_cases    ENABLE ROW LEVEL SECURITY;
ALTER TABLE test_results  ENABLE ROW LEVEL SECURITY;
ALTER TABLE load_metrics  ENABLE ROW LEVEL SECURITY;

-- ── RLS Policies ──────────────────────────────────────────────────────────────
-- The policy uses current_setting('app.current_tenant_id', TRUE):
--   - Second arg TRUE means "return NULL if not set" (instead of raising an error)
--   - We cast to UUID for type safety
--   - When the session variable is not set, current_setting returns NULL,
--     and NULL != any UUID, so NO rows are returned. This is the safe default.

CREATE POLICY tenant_isolation ON users
    USING (tenant_id = current_setting('app.current_tenant_id', TRUE)::uuid);

CREATE POLICY tenant_isolation ON projects
    USING (tenant_id = current_setting('app.current_tenant_id', TRUE)::uuid);

CREATE POLICY tenant_isolation ON api_specs
    USING (tenant_id = current_setting('app.current_tenant_id', TRUE)::uuid);

CREATE POLICY tenant_isolation ON test_runs
    USING (tenant_id = current_setting('app.current_tenant_id', TRUE)::uuid);

CREATE POLICY tenant_isolation ON test_cases
    USING (tenant_id = current_setting('app.current_tenant_id', TRUE)::uuid);

CREATE POLICY tenant_isolation ON test_results
    USING (tenant_id = current_setting('app.current_tenant_id', TRUE)::uuid);

CREATE POLICY tenant_isolation ON load_metrics
    USING (tenant_id = current_setting('app.current_tenant_id', TRUE)::uuid);

-- ── Superuser bypass (for admin operations and migrations) ────────────────────
-- The 'apiforge' superuser (used by Flyway and admin scripts) bypasses RLS.
-- Only 'apiforge_app' role is subject to RLS.
-- This is the standard Postgres pattern: migrations run as superuser, app as limited role.
