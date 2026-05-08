# Microservices Migration Guide

> This is a **planning document**, not an implementation guide. The migration has not been
> executed. Use this document to understand how to extract modules from the modular monolith
> when business triggers (see ADR-0005) are met.

---

## Target Architecture

```
                         ┌─────────────────────────────────┐
                         │          api-gateway             │
                         │  (Spring Boot, thin HTTP layer)  │
                         │  POST /api/runs → Kafka          │
                         │  GET  /api/runs/:id/events → SSE │
                         └──────────────┬──────────────────-┘
                                        │ Kafka topics
          ┌─────────────────────────────┼─────────────────────────┐
          │                             │                         │
 ┌────────▼──────────┐    ┌─────────────▼────────┐   ┌───────────▼──────────┐
 │  spec-parser-svc  │    │  executor-service     │   │  load-tester-service │
 │  (OpenAPI/Postman │    │  (WebClient, Flux,    │   │  (virtual threads,   │
 │   → Endpoint[])  │    │   auth, test results)  │   │   TimescaleDB)       │
 └────────┬──────────┘    └─────────────┬─────────┘   └──────────────────────┘
          │                             │
 ┌────────▼──────────┐    ┌─────────────▼────────┐
 │  test-gen-service │    │  reporter-service     │
 │  (11 categories,  │    │  (HTML/JSON/JUnit XML │
 │   DataGenerator)  │    │   from test_results)  │
 └───────────────────┘    └──────────────────────-┘

 Shared infrastructure: Kafka · Redis · PostgreSQL · Keycloak · Prometheus
```

---

## Module-to-Service Mapping

| Spring Modulith Module | Target Service | Owns DB Tables | Notes |
|---|---|---|---|
| `api` (thin HTTP) | `api-gateway` | none | Routes requests to downstream services via Kafka |
| `schemaparser` | `spec-parser-service` | none | Stateless; REST or gRPC for sync calls |
| `datagenerator` + `testgenerator` | `test-gen-service` | none | Stateless; collocated because they're always called together |
| `executor` + `validator` | `executor-service` | `test_runs`, `test_results` | Validator stays collocated — zero network cost |
| `loadtester` | `load-tester-service` | `load_metrics` | Separate JVM tuning; memory-heavy |
| `codegenerator` | `code-gen-service` | none | Stateless FreeMarker renderer; can stay in monolith longest |
| `reporter` | `reporter-service` | reads `test_results` (read replica) | Async; triggered by `TestRunFinishedEvent` |
| `sse` | `progress-service` | none | Redis subscriber; SSE push to browser |
| `project` + `auth` + `tenancy` + `kafka` | `project-service` | `tenants`, `projects`, `api_specs`, `users` | Core domain entities |

---

## Kafka Event Contracts

All events use Avro schemas stored in the Confluent Schema Registry.
Producers declare the schema; consumers must be compatible with it.

### Events already implemented (S09)

| Topic | Event | Producer → Consumer |
|---|---|---|
| `test-run-events` | `TestRunStartedEvent` | executor → reporter, progress-service |
| `test-run-events` | `TestCaseCompletedEvent` | executor → reporter, progress-service |
| `test-run-events` | `TestRunFinishedEvent` | executor → reporter |
| `load-metrics` | `LoadMetricSampleEvent` | load-tester → reporter |

### Events to add during extraction

| Topic | Event | Producer → Consumer | Trigger |
|---|---|---|---|
| `spec-parsed` | `SpecParsedEvent` | spec-parser → test-gen | After spec-parser-service is extracted |
| `test-cases-ready` | `TestCasesReadyEvent` | test-gen → executor | After test-gen-service is extracted |
| `report-requested` | `ReportRequestedEvent` | api-gateway → reporter | After reporter-service is extracted |
| `report-ready` | `ReportReadyEvent` | reporter → api-gateway | Notifies when async report is done |

### SpecParsedEvent schema (Avro)

```json
{
  "type": "record",
  "name": "SpecParsedEvent",
  "namespace": "io.github.shazaanashraff.apiforge.events",
  "fields": [
    { "name": "runId",      "type": "string" },
    { "name": "projectId",  "type": "string" },
    { "name": "tenantId",   "type": "string" },
    { "name": "specUrl",    "type": ["null", "string"], "default": null },
    { "name": "endpointCount", "type": "int" },
    { "name": "endpointsJson", "type": "string",
      "doc": "JSON-serialized List<Endpoint> — deserialized by test-gen-service" },
    { "name": "parsedAt",   "type": "long", "logicalType": "timestamp-millis" }
  ]
}
```

### TestCasesReadyEvent schema (Avro)

```json
{
  "type": "record",
  "name": "TestCasesReadyEvent",
  "namespace": "io.github.shazaanashraff.apiforge.events",
  "fields": [
    { "name": "runId",         "type": "string" },
    { "name": "projectId",     "type": "string" },
    { "name": "tenantId",      "type": "string" },
    { "name": "baseUrl",       "type": "string" },
    { "name": "authToken",     "type": ["null", "string"], "default": null },
    { "name": "testCaseCount", "type": "int" },
    { "name": "testCasesJson", "type": "string",
      "doc": "JSON-serialized List<TestCase>" },
    { "name": "generatedAt",   "type": "long", "logicalType": "timestamp-millis" }
  ]
}
```

---

## Data Ownership Boundaries

Each service owns its tables exclusively. Other services access foreign data via events or
dedicated read-model projections — never via cross-service JPA queries.

```
project-service owns:
  tenants          (id, slug, name, created_at)
  projects         (id, tenant_id, name, base_url, mongo_backed)
  api_specs        (id, project_id, title, format, endpoint_count, raw_json)
  users            (id, tenant_id, keycloak_id, email, role)

executor-service owns:
  test_runs        (id, project_id, tenant_id, status, started_at, finished_at)
  test_results     (id, run_id, test_case_id, path, method, category,
                    status_code, response_time_ms, passed, failure_reason)

load-tester-service owns:
  load_metrics     (run_id, sampled_at, vus, rps, p50_ms, p95_ms, p99_ms,
                    error_rate)  -- TimescaleDB hypertable
```

**Read models** (projections maintained by event consumers):

| Projection table | Maintained by | Updated from |
|---|---|---|
| `reporter_service.run_summaries` | `reporter-service` | `TestRunFinishedEvent` |
| `api_gateway.run_status_cache` | `api-gateway` (Redis) | `TestRunStartedEvent`, `TestRunFinishedEvent` |

---

## Phase 1: Extract executor-service

This is the recommended first extraction — the executor is stateless relative to the
calling context, has a clean Kafka interface, and benefits immediately from independent scaling.

### Steps

1. **Create a new Maven module** (or separate repo) `apiforge-executor-service`:
   ```
   apiforge-executor-service/
   ├── pom.xml                         ← depends on shared-api, NOT on the monolith
   ├── src/main/java/.../executor/     ← copy from monolith
   ├── src/main/java/.../validator/    ← copy from monolith (stays collocated)
   ├── src/main/java/.../kafka/        ← consumer for TestCasesReadyEvent
   └── Dockerfile
   ```

2. **Change the trigger**: instead of the monolith calling `executorService.executeAll()` directly,
   `api-gateway` publishes a `TestCasesReadyEvent` to Kafka. The executor-service consumes it.

3. **Database migration**: `test_runs` and `test_results` tables stay in the same Postgres instance
   initially; the executor-service gets its own credentials (`executor_app` role) with RLS policies.

4. **Add Kubernetes Deployment**:
   ```yaml
   apiVersion: apps/v1
   kind: Deployment
   metadata:
     name: executor-service
   spec:
     replicas: 3       # scale independently from the monolith
     template:
       spec:
         containers:
         - name: executor
           image: apiforge/executor-service:latest
           env:
           - name: SPRING_KAFKA_BOOTSTRAP_SERVERS
             valueFrom: { secretKeyRef: { name: kafka-creds, key: bootstrap } }
           - name: SPRING_DATASOURCE_URL
             valueFrom: { secretKeyRef: { name: executor-db-creds, key: url } }
   ```

5. **Update `docker-compose.yml`**: add `executor-service` service; monolith removes its
   `executor` and `validator` packages.

6. **Strangler Fig cutover**:
   - Deploy both monolith and executor-service simultaneously
   - Route 10% of runs to executor-service via a feature flag
   - Monitor error rates and latency in Grafana
   - Ramp to 100% over 2 weeks
   - Remove executor code from monolith

### Rollback Plan

If executor-service is unhealthy, the feature flag routes all traffic back to the monolith
executor synchronously. The Kafka topic retains events for 7 days — no work is lost.

---

## Phase 2: Extract reporter-service

### Steps

1. `reporter-service` consumes `TestRunFinishedEvent` from Kafka asynchronously.
2. It reads `test_results` from a **read replica** of the executor-service's Postgres.
3. Reports are stored in object storage (S3/MinIO) and a presigned URL returned via
   `ReportReadyEvent` → api-gateway → HTTP 200 with download link.
4. The synchronous HTML/JSON endpoint in the monolith is replaced with a polling endpoint
   (`GET /api/runs/{id}/report`) that checks Redis for the presigned URL.

---

## Phase 3: Extract load-tester-service

### Steps

1. `load-tester-service` consumes a new `LoadTestRequestedEvent` topic.
2. It owns the `load_metrics` TimescaleDB hypertable and publishes
   `LoadTestCompletedEvent` back when done.
3. Uses its own JVM flags: `-Xmx4g -XX:+UseZGC` (large heap for in-memory metric buffers).

---

## Phase 4: Extract spec-parser-service and test-gen-service

### Steps

1. These two are stateless — the simplest to extract.
2. `spec-parser-service`: exposes a gRPC endpoint `ParseSpec(url) → SpecParsedEvent`;
   publishes the event to Kafka after parsing.
3. `test-gen-service`: consumes `SpecParsedEvent`, generates `TestCase[]`,
   publishes `TestCasesReadyEvent`.

---

## Docker Compose Changes (Final State)

```yaml
services:
  api-gateway:
    image: apiforge/api-gateway:latest
    ports: ["8080:8080"]
    depends_on: [kafka, redis, keycloak]

  spec-parser-service:
    image: apiforge/spec-parser:latest
    depends_on: [kafka]

  test-gen-service:
    image: apiforge/test-gen:latest
    depends_on: [kafka]

  executor-service:
    image: apiforge/executor:latest
    replicas: 3
    depends_on: [kafka, postgres]

  reporter-service:
    image: apiforge/reporter:latest
    depends_on: [kafka, postgres, minio]

  load-tester-service:
    image: apiforge/load-tester:latest
    depends_on: [kafka, postgres-timescale]

  project-service:
    image: apiforge/project:latest
    depends_on: [postgres, keycloak]

  progress-service:
    image: apiforge/progress:latest
    depends_on: [redis, kafka]

  # Shared infrastructure (unchanged)
  postgres:     { image: timescale/timescaledb:latest-pg16 }
  redis:        { image: redis:7-alpine }
  kafka:        { image: confluentinc/cp-kafka:7.6.1 }
  keycloak:     { image: quay.io/keycloak/keycloak:24 }
  minio:        { image: minio/minio }
  prometheus:   { image: prom/prometheus }
  grafana:      { image: grafana/grafana }
```

---

## CI/CD Changes

Each extracted service gets its own GitHub Actions workflow:

```
.github/workflows/
  backend-ci.yml              ← monolith (shrinks as modules are extracted)
  executor-service-ci.yml     ← test + build + push Docker image
  reporter-service-ci.yml
  load-tester-service-ci.yml
  spec-parser-service-ci.yml
  test-gen-service-ci.yml
```

Each workflow:
1. `mvn test` — unit + integration tests
2. `docker build` — build image
3. `docker push` — push to registry (on `main` only)
4. Optional: `kubectl rollout` — deploy to staging

---

## Service-to-Service Authentication

After extraction, inter-service calls (Kafka consumers, gRPC) must be authenticated:

| Communication | Auth method |
|---|---|
| Kafka producer/consumer | mTLS (Confluent Cloud) or SASL/SCRAM |
| gRPC (sync spec parsing) | mTLS + service account JWT |
| REST (health checks, presigned URLs) | Internal network only (no auth needed within cluster) |
| Frontend → api-gateway | Keycloak Bearer JWT (unchanged) |

---

## Observability in the Microservices Era

All services must propagate the **W3C `traceparent` header** so distributed traces
connect across services in Tempo. The existing OTel instrumentation (added in S04) 
propagates correctly via Spring Boot auto-configuration — no code changes needed when
extracting a module.

Grafana dashboards to add after Phase 1:
- **Executor Service** — request queue depth (Kafka consumer lag), concurrent test runs,
  per-run duration, failure rate by category
- **End-to-End Trace View** — Tempo trace from `POST /api/runs` (api-gateway) through
  spec-parser → test-gen → executor → reporter

---

## Migration Readiness Checklist

Before extracting any module, verify:

- [ ] Spring Modulith `ApplicationModuleTest` passes (module boundaries enforced)
- [ ] The module has no package-private imports from other modules
- [ ] The module's public API is documented (input record, output record, events published)
- [ ] A Kafka topic exists (or is designed) for the module's primary output
- [ ] The module has unit tests covering ≥ 80% of its business logic
- [ ] A `Dockerfile` exists or can be created within 30 minutes
- [ ] A rollback plan is documented (feature flag or traffic routing)
