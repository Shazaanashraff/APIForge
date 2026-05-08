# S23 — Microservices Migration Plan

**Status:** ✅ Complete
**Section type:** Documentation only (no code changes)

## Summary

S23 documents the extraction strategy for splitting the Spring Modulith monolith into
independent microservices when scaling triggers are met. It does not execute the migration.

## Deliverables

| File | Purpose |
|---|---|
| `docs/adr/0005-microservices-extraction-strategy.md` | ADR: when/how/why to extract; phase triggers |
| `docs/microservices-migration.md` | Full migration guide: module→service mapping, Kafka event contracts, data ownership, phase-by-phase steps, Docker Compose changes, CI/CD plan, auth, observability |

## Key Decisions

- **Strangler Fig pattern** — extract one module at a time behind a feature flag, not all at once
- **Phase triggers** — extraction is demand-driven, not calendar-driven (see ADR-0005 table)
- **Phase 1: executor-service first** — highest independent value, cleanest Kafka boundary, most benefit from horizontal scaling
- **Kafka as the inter-service bus** — topics already defined in S09; new events (`SpecParsedEvent`, `TestCasesReadyEvent`, `ReportRequestedEvent`, `ReportReadyEvent`) are designed
- **Data ownership** — each service owns its tables exclusively; cross-service reads use Kafka projections or read replicas, never cross-service JPA

## Module-to-Service Summary

| Module | Service | Phase |
|---|---|---|
| `executor` + `validator` | `executor-service` | 1 |
| `reporter` | `reporter-service` | 2 |
| `loadtester` | `load-tester-service` | 3 |
| `schemaparser` | `spec-parser-service` | 4 |
| `datagenerator` + `testgenerator` | `test-gen-service` | 4 |
| `codegenerator` | `code-gen-service` | stays last |
| `sse` | `progress-service` | with Phase 1 |
| `project` + `auth` + `tenancy` | `project-service` | stays last |
| `api` | `api-gateway` | evolves throughout |

## Acceptance Criteria

- [x] ADR-0005 written and accepted
- [x] Module-to-service mapping documented
- [x] Kafka event contracts for new topics (Avro schemas) designed and documented
- [x] Data ownership boundaries defined
- [x] Phase 1 extraction steps written in detail (executor-service)
- [x] Rollback plan documented
- [x] Docker Compose final-state target documented
- [x] Migration readiness checklist written
