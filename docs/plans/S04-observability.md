# S04 — Observability Foundation

**Status:** ⬜ Pending
**Complexity:** M
**Depends on:** S01

## Goal

Wire up Micrometer metrics, OpenTelemetry distributed tracing, and structured JSON logging so that every module built in S05+ is observable from day one.

## Step-by-Step Tasks

### CP1 — Metrics (Micrometer + Prometheus)
- [ ] Verify `management.endpoints.web.exposure.include: prometheus` is in `application.yml` (already done in S01)
- [ ] Add custom `MeterRegistry` configuration: register common tags (`application`, `environment`)
- [ ] Verify Prometheus is scraping: open http://localhost:9090/targets → `apiforge-backend` shows UP
- [ ] Create first custom metric: `apiforge.schema.parse.total` counter in `SchemaParserModule` (placeholder)

### CP2 — Distributed tracing (OTel → Tempo)
- [ ] Verify OTLP exporter config in `application.yml` pointing to Tempo
- [ ] Add `@WithSpan` annotation to a sample method to verify traces appear in Grafana
- [ ] `management.tracing.sampling.probability: 1.0` (100% in dev)

### CP3 — Structured logging (Logback → Loki)
- [ ] Verify `logback-spring.xml` uses `LogstashEncoder` in `docker` profile
- [ ] Verify Promtail is picking up container logs from Docker socket
- [ ] Query in Grafana Explore: `{container="apiforge-backend"}` → logs appear
- [ ] Add `tenantId` to MDC in `TenantContextFilter` (from S03) so logs are filterable by tenant

### CP4 — Pre-built Grafana dashboard
- [ ] Create `observability/grafana/dashboards/apiforge-overview.json`:
  - Panels: HTTP request rate, error rate (5xx), p99 latency, JVM heap, active threads
- [ ] Verify dashboard auto-loads via provisioning on `docker-compose up`

## Files to Create/Modify

```
observability/grafana/dashboards/apiforge-overview.json
backend/src/main/java/.../shared/ObservabilityConfig.java
backend/src/main/java/.../shared/MetricsConstants.java
```

## Manual Verification

1. Hit `http://localhost:8081/actuator/health` 5 times
2. Open Prometheus → query `http_server_requests_seconds_count` → see values
3. Open Grafana → Dashboards → APIForge Overview → see the request count graph update
4. Open Grafana → Explore → Tempo → find a trace for the health check requests
5. Open Grafana → Explore → Loki → query `{container="apiforge-backend"}` → see logs

## Definition of Done

- [ ] All 5 manual verification steps pass
- [ ] `ObservabilityConfig` bean registered and auto-configures common tags
- [ ] Dashboard JSON committed and auto-provisioned
- [ ] PROGRESS.md updated
