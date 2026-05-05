# ADR-0001: Modular Monolith as Starting Architecture

**Date:** 2026-05-05
**Status:** Accepted
**Section:** S01

## Context

APIForge needs to be built quickly as a portfolio piece while also being designed for eventual sale as a product. A microservices architecture provides clear service boundaries and independent scaling but has high operational complexity (multiple deployments, inter-service networking, distributed tracing is mandatory from day one). A monolith is simpler to build and debug but can become a "big ball of mud" if module boundaries aren't enforced.

## Decision

Start with a **modular monolith** using Spring Modulith to enforce module boundaries. Plan and document the microservices migration (S23) without executing it. This gives clean code architecture without the operational overhead of real microservices before an MVP exists.

## Alternatives Considered

- **Microservices from day one:** Each module (executor, reporter, etc.) as a separate Spring Boot app. Rejected because: too much infrastructure overhead for a solo beginner project; debugging across service boundaries is hard without a mature observability setup.
- **Traditional monolith (no module enforcement):** Simple `@Service` classes with no boundaries. Rejected because: would become unmaintainable at scale and makes the microservices migration harder later.

## Consequences

**Positive:**
- Single deployment (`docker-compose up`) for the entire system
- Easy debugging — one JVM, one log stream
- Spring Modulith enforces boundaries at test time, so the migration to microservices is architecturally prepared
- Lower cognitive overhead for a beginner

**Negative:**
- The entire application must be redeployed for any change
- Modules share a single database (by design for now)

**Neutral:**
- S23 documents exactly how to split into microservices when the time comes
