# ADR-0005: Microservices Extraction Strategy

**Date:** 2026-05-08
**Status:** Accepted
**Section:** S23

## Context

ADR-0001 committed to starting with a modular monolith (Spring Modulith) and documenting
the microservices migration plan without executing it. The monolith is now feature-complete
(S01–S22). This ADR defines *when* to extract, *which* modules to extract first, and *how*
to do it without disrupting ongoing development.

The modular monolith has 11 domain modules:

| Module | Role | Coupling |
|---|---|---|
| `schemaparser` | Parse OpenAPI/Postman → `Endpoint[]` | Stateless, no DB |
| `datagenerator` | Generate test data (faker + boundary) | Stateless, no DB |
| `testgenerator` | Generate `TestCase[]` from `Endpoint` | Stateless, no DB |
| `executor` | Execute test cases via WebClient | Writes `test_runs`, `test_results` |
| `validator` | Validate responses (status/schema/SLA) | Stateless |
| `loadtester` | Virtual-thread load testing | Writes `load_metrics` hypertable |
| `codegenerator` | Render runnable test code (FreeMarker) | Stateless, no DB |
| `reporter` | Generate HTML/JSON/JUnit XML reports | Reads `test_results` |
| `sse` | Redis pub/sub → SSE streaming | Redis only |
| `project` | Project + tenant CRUD | Owns `projects`, `tenants`, `api_specs` |
| `api` | HTTP API layer (controllers) | Thin orchestration |
| `kafka` | Avro event publishing | Cross-cutting |

Three modules are the best first extraction candidates because they are stateless, have
clearly bounded inputs and outputs, and carry the highest independent value:
- `executor` (can be scaled independently for concurrent test runs)
- `loadtester` (CPU/memory intensive — benefits most from dedicated resources)
- `reporter` (read-heavy; can be horizontally scaled independently)

## Decision

Extract modules into independent services using the **Strangler Fig pattern** in four
phases, triggered by business need rather than a fixed schedule:

| Phase | Trigger | Services extracted |
|---|---|---|
| 1 | Concurrent test runs > 10/min saturate the executor thread pool | `executor-service` |
| 2 | Report generation adds > 500 ms to the test run response time | `reporter-service` |
| 3 | Load tests impact executor response times | `load-tester-service` |
| 4 | Spec parsing becomes a bottleneck for parallel team usage | `spec-parser-service` + `test-gen-service` |

All inter-service communication flows through **Kafka** (async, event-driven) except for
the synchronous spec-parse path (which stays request/response via gRPC or REST).

## Alternatives Considered

- **Extract all modules at once:** Rejected. Too risky — 11 simultaneous moving parts,
  difficult to reason about failure modes, no business value until all are done.
- **Domain-driven decomposition by business capability:** Considered. The current module
  boundaries already align with DDD aggregates, making module = service the natural
  mapping. Adopted this implicitly.
- **gRPC for all inter-service communication:** Rejected for Phase 1. Kafka already
  provides durability, replay, and fan-out. Synchronous gRPC is added only where
  latency is critical (spec parsing is synchronous by nature).
- **Stay monolith forever:** Valid option if load requirements never exceed single-instance
  capacity. The ADR does not mandate extraction — it documents how to do it safely if needed.

## Consequences

**Positive:**
- Each extracted service can be scaled, deployed, and rolled back independently
- `executor-service` and `load-tester-service` can use different JVM tuning
  (executor: many virtual threads; load-tester: large heap for in-memory metric buffers)
- Failure in `reporter-service` doesn't affect test execution
- Teams can own individual services independently

**Negative:**
- Each extraction adds operational complexity: new Kubernetes Deployment, new Dockerfile,
  new CI pipeline, new health checks, new distributed tracing spans
- Kafka-based async means eventual consistency — the UI cannot show results
  synchronously after the first POST /api/runs call once executor is async
- Service-to-service authentication must be added (mTLS or internal JWT)

**Neutral:**
- Spring Modulith's module verification tests (`ApplicationModuleTest`) ensure the current
  monolith's module boundaries are enforced — these become the service contracts
- The Kafka topics introduced in S09 are already the inter-service event bus;
  no new messaging infrastructure is required
