# APIForge — Master Plan

> High-level section roadmap. For granular tasks, see `docs/plans/SXX-*.md`.
> This document is the source of truth for what gets built and in what order.

---

## Phase 1 — MVP (S01–S22)

| ID | Section | Complexity | Key Output |
|---|---|---|---|
| S01 | Project Skeleton & Docs Scaffold | M | Repo structure, all docs, Docker Compose, CI skeleton |
| S02 | Database & Persistence | M | Postgres+TimescaleDB, Flyway, JPA entities, RLS scaffolding |
| S03 | Auth & Multi-Tenancy | L | Spring Security + Keycloak JWT, tenant context, RLS enforcement |
| S04 | Observability Foundation | M | Prometheus, Grafana, Loki, Tempo, OTel traces |
| S05 | Schema Parser Module | L | OpenAPI 3.x + Postman → internal `Endpoint[]` model |
| S06 | Data Generator Module | M | Faker + JQwik + ObjectId generators |
| S07 | Test Case Generator Module | XL | All 11 test categories |
| S08 | Code Generator Module | L | JUnit5/RestAssured + Jest/Supertest + k6 + Gatling output |
| S09 | Kafka Event Backbone | M | KRaft + Schema Registry + Avro events |
| S10 | Executor Module | L | Reactive WebClient + chaining + auth refresh |
| S11 | Validator Module | M | Status + JSON schema + SLA + headers |
| S12 | Load Tester Module | L | Virtual threads + scenarios + TimescaleDB metrics |
| S13 | Reporter Module | M | HTML (by category) + JSON + JUnit XML |
| S14 | REST API Layer | M | Spring Web controllers + SpringDoc + RFC 7807 |
| S15 | Real-Time Progress (SSE) | S | SSE endpoint + Redis pub/sub |
| S16 | Frontend — Foundation | M | React+Vite+TS+Tailwind, Keycloak OIDC, API client |
| S17 | Frontend — Spec & Project Mgmt | M | Monaco editor, project CRUD, Mongo flag toggle |
| S18 | Frontend — Test Execution UI | M | Category selector, SSE progress stream |
| S19 | Frontend — Reports & Viz | M | Recharts + ECharts latency graphs |
| S20 | Sample Buggy APIs | L | Java + Node sample APIs with documented bugs |
| S21 | End-to-End Integration | M | Full E2E flow: Petstore + both sample APIs + Postman |
| S22 | Polish & Documentation | M | README, screenshots, demo GIFs, LEARNING.md |

## Phase 2 — Real Product (S23+)

| ID | Section | Purpose |
|---|---|---|
| S23 | Microservices Migration Plan | Document (not execute) extraction of executor/load-tester/reporter |

---

## Build Order Rationale

Foundations → Schema → Generation → Execution → Reporting → API → Frontend → Demo → Polish

See the full rationale in `S:\apiforge\master-prompt-zazzy-manatee.md`.

---

## Definition of Done (per checkpoint)

All 5 required:
1. Code compiles and runs
2. Tests pass
3. Docs updated + ADR if architectural
4. Conventional Commits commit
5. PROGRESS.md updated
