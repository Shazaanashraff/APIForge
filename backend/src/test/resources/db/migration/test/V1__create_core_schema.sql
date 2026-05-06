-- Test migration: same as main V1 but without TimescaleDB dependency.
-- Copied from main V1; the load_metrics table uses a standard composite PK
-- that doesn't require the extension.

CREATE TABLE tenants (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(255) NOT NULL,
    slug       VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE users (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id      UUID        NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    keycloak_id    VARCHAR(255) NOT NULL UNIQUE,
    email          VARCHAR(255) NOT NULL,
    name           VARCHAR(255),
    role           VARCHAR(50)  NOT NULL DEFAULT 'USER',
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE projects (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID         NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    name                VARCHAR(255) NOT NULL,
    description         TEXT,
    base_url            VARCHAR(2048),
    is_mongo_backed_api BOOLEAN      NOT NULL DEFAULT false,
    created_by          UUID         REFERENCES users(id),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE api_specs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID        NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    project_id      UUID        NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    source_type     VARCHAR(50) NOT NULL,
    source_url      VARCHAR(2048),
    spec_format     VARCHAR(20) NOT NULL,
    spec_content    TEXT        NOT NULL,
    endpoint_count  INTEGER,
    parsed_at       TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE test_runs (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID        NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    project_id          UUID        NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    api_spec_id         UUID        REFERENCES api_specs(id),
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    categories_selected TEXT[],
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

CREATE TABLE test_cases (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id               UUID         NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    test_run_id             UUID         NOT NULL REFERENCES test_runs(id) ON DELETE CASCADE,
    category                VARCHAR(30)  NOT NULL,
    endpoint_path           VARCHAR(500) NOT NULL,
    http_method             VARCHAR(10)  NOT NULL,
    description             TEXT,
    request_headers         JSONB,
    request_query_params    JSONB,
    request_path_params     JSONB,
    request_body            TEXT,
    expected_status_codes   INTEGER[],
    expected_assertions     JSONB,
    applicable_if_mongo     BOOLEAN      NOT NULL DEFAULT false,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now()
);

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
    failure_reason      TEXT,
    assertion_results   JSONB,
    executed_at         TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE load_metrics (
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    tenant_id       UUID        NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    test_run_id     UUID        NOT NULL REFERENCES test_runs(id) ON DELETE CASCADE,
    sampled_at      TIMESTAMPTZ NOT NULL,
    rps             NUMERIC(10,2),
    active_vus      INTEGER,
    p50_ms          BIGINT,
    p95_ms          BIGINT,
    p99_ms          BIGINT,
    max_ms          BIGINT,
    error_rate      NUMERIC(5,4),
    total_requests  BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (id, sampled_at)
);

INSERT INTO tenants (id, name, slug)
VALUES ('00000000-0000-0000-0000-000000000001', 'Demo Organisation', 'demo');
