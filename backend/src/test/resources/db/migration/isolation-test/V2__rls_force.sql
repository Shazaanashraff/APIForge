-- ─────────────────────────────────────────────────────────────────────────────
-- Test-only migration: enable FORCE RLS on the projects table so that even
-- the table owner (the Testcontainers "apiforge" user) is subject to the policy.
--
-- We use FOR SELECT only, so test setup INSERTs are unrestricted.
-- The policy mirrors V3 in the main migration set.
-- ─────────────────────────────────────────────────────────────────────────────

ALTER TABLE projects ENABLE ROW LEVEL SECURITY;
-- FORCE means even the table owner cannot bypass the policy.
ALTER TABLE projects FORCE ROW LEVEL SECURITY;

-- Only restrict SELECT — INSERT/UPDATE/DELETE are unrestricted so tests can
-- set up data freely without needing the session variable to be set first.
CREATE POLICY tenant_isolation ON projects
    FOR SELECT
    USING (tenant_id = current_setting('app.current_tenant_id', TRUE)::uuid);
