# APIForge — Progress Tracker

> This is the **resume file**. Read it at the start of every session.
> Update it before stopping work for any reason — even mid-task.

---

## 🔖 LAST CHECKPOINT

- **Date:** 2026-05-05
- **Section:** S01 — Project Skeleton & Docs Scaffold
- **Checkpoint ID:** S01-CP1
- **Last commit:** (pending — not yet committed)
- **Next file to work on:** `DEVELOPER_SETUP.md`
- **Resume instructions:** S01-CP1 is in progress. README.md, CLAUDE.md, and PROGRESS.md have been written. Continue with S01-CP2: write DEVELOPER_SETUP.md, RUNBOOK.md, CONTRIBUTING.md, LEARNING.md, LICENSE. Then S01-CP3: .gitignore, .env.example, docker-compose.yml, docker-compose.lite.yml.

---

## ✅ COMPLETED CHECKPOINTS

*(none yet)*

---

## 🚧 IN PROGRESS

- [ ] **S01-CP1**: Core session docs (README, CLAUDE.md, PROGRESS.md)
- [ ] **S01-CP2**: User-facing docs (DEVELOPER_SETUP.md, RUNBOOK.md, CONTRIBUTING.md, LEARNING.md, LICENSE)
- [ ] **S01-CP3**: Config + Docker Compose (.gitignore, .env.example, docker-compose.yml, docker-compose.lite.yml)
- [ ] **S01-CP4**: Backend Maven skeleton (pom.xml, application.yml, ApiForgeApplication.java, module stubs)
- [ ] **S01-CP5**: Frontend skeleton (package.json, vite.config.ts, src/ stubs)
- [ ] **S01-CP6**: Sample API skeletons (Java + Node)
- [ ] **S01-CP7**: Observability configs + GitHub Actions + PowerShell scripts
- [ ] **S01-CP8**: Per-section detailed plans (docs/plans/S01–S23) + initial ADRs

---

## 📋 UPCOMING (NEXT 5 SECTIONS)

- [ ] **S02**: Database & Persistence (Postgres + TimescaleDB + Flyway + RLS)
- [ ] **S03**: Auth & Multi-Tenancy (Spring Security + JWT + Keycloak)
- [ ] **S04**: Observability Foundation (Micrometer + Prometheus + Grafana)
- [ ] **S05**: Schema Parser Module (OpenAPI + Postman → internal model)
- [ ] **S06**: Data Generator Module (Faker + JQwik + ObjectId)

---

## 🚫 BLOCKERS

*(none currently)*

---

## 📝 NOTES FOR NEXT SESSION

- GitHub username: `Shazaanashraff`
- GitHub repo: `https://github.com/Shazaanashraff/APIForge.git`
- Java package root: `io.github.shazaanashraff.apiforge`
- Project root: `S:\apiforge\`
- Auth schemes for generated tests (MVP): Bearer JWT + API key + Basic
- Phase 1 = S01–S22 (full working tool), Phase 2 = S23+ (microservices plan + hardening)
- Keycloak runs in Docker Compose from day one; multi-tenancy scaffolded but defaults to single tenant
- Local dev: deps (postgres, redis, mongo) in Docker; backend via `.\mvnw.cmd spring-boot:run`; frontend via `npm run dev`
- Token budget concern: size checkpoints small enough to finish in one sitting

---

## 📊 SECTION COMPLETION SUMMARY

| ID | Section | Status | Commit |
|---|---|---|---|
| S01 | Project Skeleton | 🔄 In Progress | — |
| S02 | Database & Persistence | ⬜ Pending | — |
| S03 | Auth & Multi-Tenancy | ⬜ Pending | — |
| S04 | Observability Foundation | ⬜ Pending | — |
| S05 | Schema Parser Module | ⬜ Pending | — |
| S06 | Data Generator Module | ⬜ Pending | — |
| S07 | Test Case Generator Module | ⬜ Pending | — |
| S08 | Code Generator Module | ⬜ Pending | — |
| S09 | Kafka Event Backbone | ⬜ Pending | — |
| S10 | Executor Module | ⬜ Pending | — |
| S11 | Validator Module | ⬜ Pending | — |
| S12 | Load Tester Module | ⬜ Pending | — |
| S13 | Reporter Module | ⬜ Pending | — |
| S14 | REST API Layer | ⬜ Pending | — |
| S15 | Real-Time Progress (SSE) | ⬜ Pending | — |
| S16 | Frontend — Foundation | ⬜ Pending | — |
| S17 | Frontend — Spec & Project Mgmt | ⬜ Pending | — |
| S18 | Frontend — Test Execution UI | ⬜ Pending | — |
| S19 | Frontend — Reports & Viz | ⬜ Pending | — |
| S20 | Sample Buggy APIs | ⬜ Pending | — |
| S21 | End-to-End Integration | ⬜ Pending | — |
| S22 | Polish & Documentation | ⬜ Pending | — |
| S23 | Microservices Migration Plan | ⬜ Pending | — |
