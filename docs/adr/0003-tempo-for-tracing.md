# ADR-0003: Grafana Tempo for Distributed Tracing

**Date:** 2026-05-05
**Status:** Accepted
**Section:** S01 / S04

## Context

APIForge needs a distributed tracing backend to store and visualize OpenTelemetry spans. The main candidates are Grafana Tempo and Jaeger.

## Decision

Use **Grafana Tempo**. It integrates natively with Grafana (same ecosystem as Loki and Prometheus that are already in the stack), supports correlating traces with logs and metrics from a single Grafana UI, and requires zero configuration for basic use.

## Alternatives Considered

- **Jaeger:** Mature, widely used, has its own rich UI. Rejected because: adds a separate UI to manage (Jaeger UI at a different port) and native Grafana integration requires a plugin, while Tempo is a first-class Grafana datasource.
- **Zipkin:** Older, less actively maintained, no native Grafana datasource. Rejected.

## Consequences

**Positive:**
- Unified observability in a single Grafana instance (metrics + logs + traces)
- Trace-to-log correlation works out of the box with Loki datasource configured
- Minimal Docker Compose configuration

**Negative:**
- Tempo's UI is accessed via Grafana — not a standalone rich UI like Jaeger's
- Less community documentation than Jaeger for advanced use cases

**Neutral:**
- Both accept standard OTLP — switching later is a config change, not a code change
