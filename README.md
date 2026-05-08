# APIForge

> Automatically generate and execute test suites for any REST API — from an OpenAPI schema, Postman collection, or live server introspection.

[![Backend CI](https://github.com/Shazaanashraff/APIForge/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/Shazaanashraff/APIForge/actions/workflows/backend-ci.yml)
[![Frontend CI](https://github.com/Shazaanashraff/APIForge/actions/workflows/frontend-ci.yml/badge.svg)](https://github.com/Shazaanashraff/APIForge/actions/workflows/frontend-ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## What is APIForge?

APIForge reads your API's OpenAPI 3.x spec (or Postman v2.1 collection) and:

1. **Generates** test cases across **11 categories** automatically
2. **Renders** runnable test code in JUnit 5 + RestAssured, Jest + Supertest, k6, and Gatling
3. **Executes** the tests with real HTTP calls — including chained requests (login → use token), auth refresh, and configurable concurrency
4. **Validates** responses: status codes, JSON schema, headers, and per-endpoint response-time SLAs
5. **Runs** load tests on Java 21 virtual threads
6. **Reports** in HTML (grouped by category), JSON, and JUnit XML

### The 11 Test Categories

| # | Category | What It Catches |
|---|---|---|
| 1 | Happy path | Correct inputs → correct 2xx responses |
| 2 | Boundary | Min/max values, empty strings, max-length strings |
| 3 | Negative | Wrong types, missing required fields, invalid enums |
| 4 | Auth | Missing token, expired token, wrong scope |
| 5 | Security | SQL injection, XSS, path traversal, NoSQL injection |
| 6 | Idempotency | PUT/DELETE called twice → same result |
| 7 | Rate limit | Burst requests → expected throttling response |
| 8 | Performance SLA | Per-endpoint response-time threshold violations |
| 9 | Payload size | Empty bodies, just-over-max payloads, 10x-max payloads |
| 10 | Pagination | Offset/page/cursor styles, edge cases, metadata validation |
| 11 | MongoDB-specific | ObjectId format validation, NoSQL injection via query params |

---

## APIForge in Action

Point APIForge at the bundled **Node.js sample API** (which has 10 deliberate bugs) and run a test suite:

```
POST /api/runs
{
  "specUrl": "http://localhost:3000/api-docs/json",
  "baseUrl": "http://localhost:3000",
  "projectId": "demo",
  "tenantId":  "default"
}
```

APIForge parses the spec, generates ~60 test cases across all 11 categories, executes them, and returns results in seconds. Here's a sample of bugs it detects:

| Bug in Sample API | Category Detected By | Failure Reason |
|---|---|---|
| `GET /users/not-an-id` → 500 (CastError unhandled) | **Negative** | Expected 400, got 500 |
| `GET /users?role[$ne]=admin` bypasses filter | **Security** | NoSQL injection accepted (200) |
| `POST /users/login` with `{"username":{"$gt":""}}` | **Security** | Operator injection accepted (200) |
| `GET /products` returns `size+1` items | **Pagination** | Off-by-one in pagination |
| `GET /products` missing `total` field | **Pagination** | Response schema incomplete |
| `DELETE /products/:id` twice → 500 | **Idempotency** | Second delete crashes (500) |
| `POST /products` with empty body → 500 | **Negative** | Expected 400, got 500 |
| `GET /products?size=999999` returns full collection | **Payload size** | No limit cap enforced |

The same run against the **Java sample API** catches:
- `GET /products/:id` returning 200 (spec says 201 on `POST /products`)
- `Thread.sleep(3000)` in `GET /products/slow` breaching the 2 s SLA
- `DELETE /products/:id` second call → 500 (findById + `.get()` on empty Optional)
- `GET /admin/stats` accessible without any authentication (missing `@PreAuthorize`)

---

## Architecture

```
┌────────────────────────────────────────────────────────────────────┐
│                         APIForge Backend                           │
│                  (Spring Boot 3 · Java 21 · Modulith)              │
│                                                                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐             │
│  │ schemaparser │→ │  testgen     │→ │  executor    │             │
│  │ (OpenAPI /   │  │ (11 category │  │ (WebClient,  │             │
│  │  Postman)    │  │  generators) │  │  Flux, auth) │             │
│  └──────────────┘  └──────────────┘  └──────┬───────┘             │
│                                             │                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────▼───────┐             │
│  │  codegen     │  │  reporter    │  │  validator   │             │
│  │ (RestAssured │  │ (HTML/JSON/  │  │ (status,     │             │
│  │  /K6/Gatling)│  │  JUnit XML)  │  │  schema, SLA)│             │
│  └──────────────┘  └──────────────┘  └──────────────┘             │
│                                                                    │
│  Infrastructure: Kafka · Redis SSE · Postgres · TimescaleDB        │
└────────────────────────────────────────────────────────────────────┘
         │                                      │
   ┌─────▼──────┐                       ┌───────▼──────┐
   │  Keycloak  │                       │  Frontend    │
   │ (OIDC/JWT) │                       │ (React+Vite) │
   └────────────┘                       └──────────────┘
```

## Project Structure

```
apiforge/
├── backend/                    # Spring Boot 3 + Java 21 modular monolith
│   └── src/main/java/.../modules/
│       ├── schemaparser/       # OpenAPI + Postman parsing
│       ├── testgenerator/      # 11-category test case generation
│       ├── datagenerator/      # Realistic + boundary test data
│       ├── executor/           # Reactive HTTP test execution
│       ├── validator/          # Status code, schema, SLA validation
│       ├── codegenerator/      # Renders runnable test code
│       ├── loadtester/         # Virtual-thread load testing
│       ├── reporter/           # HTML / JSON / JUnit XML reports
│       ├── sse/                # Real-time progress via Redis pub/sub
│       ├── kafka/              # Event backbone (Avro schemas)
│       ├── project/            # Project management
│       └── api/                # REST API layer
├── frontend/                   # React 18 + Vite + TypeScript + Tailwind
├── sample-target-api-java/     # Buggy Spring Boot + PostgreSQL demo API
├── sample-target-api-node/     # Buggy Node.js + Express + MongoDB demo API
├── docs/                       # Plans, ADRs, Postman collection
├── observability/              # Prometheus, Grafana, Loki, Tempo configs
├── scripts/                    # PowerShell utilities + smoke-test.ps1
└── docker-compose.yml          # Full local stack (13+ services)
```

## Tech Stack Highlights

- **Backend**: Spring Boot 3.x · Java 21 virtual threads · Spring Modulith · Spring WebFlux
- **Auth**: Keycloak (OAuth2/OIDC) · Spring Security · Postgres RLS for multi-tenancy
- **Storage**: PostgreSQL 16 + TimescaleDB · Redis 7 · Kafka (KRaft)
- **Observability**: Prometheus · Grafana · Loki · Tempo · OpenTelemetry
- **Frontend**: React 18 · Vite · TypeScript · Tailwind · Monaco Editor · Recharts

---

## Getting Started

See [DEVELOPER_SETUP.md](DEVELOPER_SETUP.md) for full prerequisites and first-time setup.

```powershell
# Clone
git clone https://github.com/Shazaanashraff/APIForge.git
cd apiforge

# Copy env template
copy .env.example .env
# Edit .env with your values

# Start dependencies (deps-only dev mode)
docker-compose -f docker-compose.lite.yml up -d

# Start backend
cd backend && .\mvnw.cmd spring-boot:run

# Start frontend (new terminal)
cd frontend && npm run dev
```

Full stack including observability and Keycloak:
```powershell
docker-compose up -d
```

See [RUNBOOK.md](RUNBOOK.md) for health check URLs, common errors, and smoke tests.

---

## Documentation

| Document | Purpose |
|---|---|
| [DEVELOPER_SETUP.md](DEVELOPER_SETUP.md) | Prerequisites + first-time setup |
| [RUNBOOK.md](RUNBOOK.md) | Running, verifying, and troubleshooting the system |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Git workflow and commit conventions |
| [docs/MASTER_PLAN.md](docs/MASTER_PLAN.md) | Overall project plan and section roadmap |
| [docs/adr/](docs/adr/) | Architecture Decision Records |
| [LEARNING.md](LEARNING.md) | Concepts and resources introduced in this project |

---

## Running the Test Suite

```powershell
# Backend unit tests (no Docker needed)
cd backend && .\mvnw.cmd test

# Frontend tests
cd frontend && npm test

# Full pipeline E2E test (MockWebServer, no external services)
cd backend && .\mvnw.cmd test "-Dtest=PipelineE2ETest"

# Smoke test (requires APIForge backend + Node sample API running)
.\scripts\smoke-test.ps1
```

## Key Technical Decisions

| Decision | Choice | Rationale |
|---|---|---|
| HTTP test execution | Spring WebFlux `WebClient` + `Flux.flatMap` | Non-blocking; configurable concurrency without thread explosion |
| Multi-tenancy | Postgres RLS + `SET LOCAL app.current_tenant_id` | Isolation at DB layer; no risk of accidental cross-tenant leaks |
| SSE progress streaming | Redis pub/sub → `SseEmitter` | Decouples executor from HTTP layer; scales across instances |
| Test data generation | Seeded `DataGenerator` (Datafaker + custom generators) | Reproducible test runs for debugging |
| Frontend state | Zustand (client state) + React Query (server state) | Clean separation; avoids Redux boilerplate |

See [docs/adr/](docs/adr/) for full Architecture Decision Records.

## Status

S21 of S23 complete — core tool is fully functional. See [PROGRESS.md](PROGRESS.md) for the current checkpoint.

---

## License

[MIT](LICENSE) © 2026 Shazaanashraff
