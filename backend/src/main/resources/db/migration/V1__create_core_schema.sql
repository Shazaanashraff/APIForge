-- ─────────────────────────────────────────────────────────────────────────────
-- V1: Core APIForge schema
--
-- All tenant-scoped tables carry:
--   tenant_id UUID NOT NULL  — used by V3 RLS policies
--   created_at / updated_at  — auto-managed by application layer
-- ─────────────────────────────────────────────────────────────────────────────

-- ── Tenants ──────────────────────────────────────────────────────────────────
-- An organisation that owns projects. In Phase 1 a single demo tenant is seeded.
CREATE TABLE tenants (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(255) NOT NULL,
    slug       VARCHAR(100) NOT NULL UNIQUE,  -- URL-safe identifier
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ── Users ─────────────────────────────────────────────────────────────────────
-- Linked to a tenant. Auth is handled by Keycloak; this table mirrors the
-- Keycloak user ID so we can do JOINs and audit logs locally.
CREATE TABLE users (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id      UUID        NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    keycloak_id    VARCHAR(255) NOT NULL UNIQUE,  -- sub claim from JWT
    email          VARCHAR(255) NOT NULL,
    name           VARCHAR(255),
    role           VARCHAR(50)  NOT NULL DEFAULT 'USER',  -- USER | ADMIN
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_users_tenant_id     ON users(tenant_id);
CREATE INDEX idx_users_keycloak_id   ON users(keycloak_id);

-- ── Projects ──────────────────────────────────────────────────────────────────
-- A project groups an API spec + its test runs. One project per target API.
CREATE TABLE projects (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID         NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    name                VARCHAR(255) NOT NULL,
    description         TEXT,
    base_url            VARCHAR(2048),          -- e.g. http://localhost:8090
    is_mongo_backed_api BOOLEAN      NOT NULL DEFAULT false,
    created_by          UUID         REFERENCES users(id),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_projects_tenant_id ON projects(tenant_id);

-- ── API Specs ─────────────────────────────────────────────────────────────────
-- Stores the raw spec content (OpenAPI JSON/YAML or Postman collection JSON).
-- One-to-one with a project (latest spec wins; previous kept for history).
CREATE TABLE api_specs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID        NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    project_id      UUID        NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    source_type     VARCHAR(50) NOT NULL,  -- FILE | URL | INTROSPECT | POSTMAN
    source_url      VARCHAR(2048),
    spec_format     VARCHAR(20) NOT NULL,  -- OPENAPI_JSON | OPENAPI_YAML | POSTMAN
    spec_content    TEXT        NOT NULL,  -- raw spec text
    endpoint_count  INTEGER,
    parsed_at       TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_api_specs_project_id ON api_specs(project_id);
CREATE INDEX idx_api_specs_tenant_id  ON api_specs(tenant_id);

-- ── Test Runs ─────────────────────────────────────────────────────────────────
-- One test run = one execution of the test suite against the target API.
CREATE TABLE test_runs (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID        NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    project_id          UUID        NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    api_spec_id         UUID        REFERENCES api_specs(id),
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                    -- PENDING | RUNNING | COMPLETED | FAILED | CANCELLED
    categories_selected TEXT[],     -- subset of 11 categories the user chose to run
    concurrency         INTEGER     NOT NULL DEFAULT 1,
    base_url            VARCHAR(2048),
    total_cases         INTEGER,
    passed_cases        INTEGER,
    failed_cases        INTEGER,
    skipped_cases       INTEGER,
    started_at          TIMESTAMPTZ,
    completed_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_test_runs_project_id ON test_runs(project_id);
CREATE INDEX idx_test_runs_tenant_id  ON test_runs(tenant_id);
CREATE INDEX idx_test_runs_status     ON test_runs(status);

-- ── Test Cases ────────────────────────────────────────────────────────────────
-- One test case = a single HTTP request + expected assertions.
-- Generated from endpoints + test category.
CREATE TABLE test_cases (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id               UUID         NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    test_run_id             UUID         NOT NULL REFERENCES test_runs(id) ON DELETE CASCADE,
    category                VARCHAR(30)  NOT NULL,
                                         -- HAPPY_PATH | BOUNDARY | NEGATIVE | AUTH | SECURITY |
                                         -- IDEMPOTENCY | RATE_LIMIT | PERFORMANCE_SLA |
                                         -- PAYLOAD_SIZE | PAGINATION | MONGODB_SPECIFIC
    endpoint_path           VARCHAR(500) NOT NULL,
    http_method             VARCHAR(10)  NOT NULL,
    description             TEXT,
    request_headers         JSONB,
    request_query_params    JSONB,
    request_path_params     JSONB,
    request_body            TEXT,
    expected_status_codes   INTEGER[],
    expected_assertions     JSONB,       -- list of assertion descriptors
    applicable_if_mongo     BOOLEAN      NOT NULL DEFAULT false,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_test_cases_test_run_id ON test_cases(test_run_id);
CREATE INDEX idx_test_cases_category    ON test_cases(category);
CREATE INDEX idx_test_cases_tenant_id   ON test_cases(tenant_id);

-- ── Test Results ──────────────────────────────────────────────────────────────
-- One result per test case execution.
CREATE TABLE test_results (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID         NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    test_run_id         UUID         NOT NULL REFERENCES test_runs(id) ON DELETE CASCADE,
    test_case_id        UUID         NOT NULL REFERENCES test_cases(id) ON DELETE CASCADE,
    passed              BOOLEAN      NOT NULL,
    actual_status_code  INTEGER,
    response_time_ms    BIGINT,
    response_headers    JSONB,
    response_body       TEXT,
    failure_reason      TEXT,        -- human-readable explanation of why it failed
    assertion_results   JSONB,       -- per-assertion pass/fail details
    executed_at         TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_test_results_test_run_id  ON test_results(test_run_id);
CREATE INDEX idx_test_results_test_case_id ON test_results(test_case_id);
CREATE INDEX idx_test_results_tenant_id    ON test_results(tenant_id);
-- Fast grouped-by-category query for the report:
CREATE INDEX idx_test_results_run_category
    ON test_results(test_run_id)
    INCLUDE (passed);

-- ── Load Metrics ──────────────────────────────────────────────────────────────
-- Time-series samples from load test runs. This table becomes a TimescaleDB
-- hypertable in V2 (after the extension is enabled).
CREATE TABLE load_metrics (
    id              UUID    DEFAULT gen_random_uuid(),
    tenant_id       UUID    NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    test_run_id     UUID    NOT NULL REFERENCES test_runs(id) ON DELETE CASCADE,
    sampled_at      TIMESTAMPTZ NOT NULL,  -- partition key for TimescaleDB
    rps             NUMERIC(10,2),         -- requests per second
    active_vus      INTEGER,               -- virtual users active at sample time
    p50_ms          BIGINT,                -- 50th percentile latency
    p95_ms          BIGINT,                -- 95th percentile latency
    p99_ms          BIGINT,                -- 99th percentile latency
    max_ms          BIGINT,
    error_rate      NUMERIC(5,4),          -- 0.0 to 1.0
    total_requests  BIGINT,
    PRIMARY KEY (id, sampled_at)           -- composite PK required for TimescaleDB
);
CREATE INDEX idx_load_metrics_test_run_id ON load_metrics(test_run_id, sampled_at DESC);

-- ── Seed Data ─────────────────────────────────────────────────────────────────
-- A single demo tenant for Phase 1. All API calls default to this tenant.
INSERT INTO tenants (id, name, slug)
VALUES ('00000000-0000-0000-0000-000000000001', 'Demo Organisation', 'demo');
