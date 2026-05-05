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

## Quick Demo

*Coming soon — demo GIFs showing APIForge finding real bugs in both sample APIs.*

---

## Project Structure

```
apiforge/
├── backend/                    # Spring Boot 3 + Java 21 modular monolith
├── frontend/                   # React 18 + Vite + TypeScript + Tailwind
├── sample-target-api-java/     # Buggy Spring Boot + PostgreSQL demo API
├── sample-target-api-node/     # Buggy Node.js + Express + MongoDB demo API
├── docs/                       # Plans, ADRs, architecture diagrams
├── observability/              # Prometheus, Grafana, Loki, Tempo configs
├── scripts/                    # Windows PowerShell utility scripts
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

## Status

This project is actively being built. See [PROGRESS.md](PROGRESS.md) for the current checkpoint.

---

## License

[MIT](LICENSE) © 2026 Shazaanashraff
