# ADR-0002: PostgreSQL 16 with TimescaleDB Extension

**Date:** 2026-05-05
**Status:** Accepted
**Section:** S01 / S02

## Context

APIForge needs to store: (a) relational data (projects, test runs, results, users) and (b) time-series metrics from load tests (latency samples at high frequency). These are two different data access patterns.

## Decision

Use **PostgreSQL 16 with the TimescaleDB extension** for both. TimescaleDB adds a `hypertable` type that automatically partitions time-series data by time, enabling fast range queries over metrics without a separate database engine.

## Alternatives Considered

- **PostgreSQL + InfluxDB:** Two separate databases — more operational complexity. Rejected.
- **PostgreSQL + TimescaleDB + MongoDB for APIForge:** MongoDB for document storage of test cases. Rejected — the relational model fits our data better and MongoDB is already in the stack for the sample API.
- **MySQL:** Lacks window functions and JSON operators that we use for test result queries. Rejected.

## Consequences

**Positive:**
- Single database for both relational and time-series workloads
- TimescaleDB hypertables are transparent to JPA — accessed as regular tables with a thin Micrometer extension
- Strong Spring Data / Flyway / HikariCP ecosystem support

**Negative:**
- TimescaleDB extension requires the `timescale/timescaledb` Docker image, not the standard `postgres` image
- Minor setup complexity to enable the extension in Flyway migrations

**Neutral:**
- TimescaleDB is PostgreSQL-compatible — any Postgres tool works unchanged
