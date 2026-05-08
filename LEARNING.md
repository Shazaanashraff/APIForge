# LEARNING.md — Concepts Introduced in APIForge

> This file tracks every significant new concept introduced as the project is built, in the order it appeared. For each concept, there's a one-sentence explanation and a link to a deeper resource.

---

## How to Use This File

When a new library or architectural concept appears in the code for the first time, an entry is added here. If you encounter something in the code you don't understand, search this file first.

---

## S01 — Project Skeleton

### Conventional Commits
A standard format for git commit messages: `type(scope): subject`. Makes changelogs automatable and history readable.
→ https://www.conventionalcommits.org/

### Maven Wrapper (`mvnw.cmd`)
A script that downloads the exact Maven version the project needs, so you don't have to install Maven globally.
→ https://maven.apache.org/wrapper/

### Docker Compose
A tool for defining and running multi-container Docker apps from a single YAML file. We use it to run Postgres, Redis, Kafka, Keycloak, etc. locally.
→ https://docs.docker.com/compose/

### `.env` files
A convention for storing environment-specific configuration (passwords, URLs) outside of code. Never committed. `.env.example` is the template that IS committed.
→ https://12factor.net/config

### Architecture Decision Records (ADRs)
Short documents capturing WHY a significant technical decision was made. Once accepted, they're immutable — superseded, never edited.
→ https://adr.github.io/

---

## S02 — Database & Persistence

*(entries added during S02)*

### Flyway
Database migration tool. Instead of modifying the database by hand, you write SQL scripts (migrations) that Flyway runs in order. This means the schema is reproducible and version-controlled.
→ https://flywaydb.org/documentation/

### TimescaleDB
A PostgreSQL extension that adds time-series capabilities. We use it to store load-test metrics (latency over time) efficiently.
→ https://docs.timescale.com/

### Postgres Row-Level Security (RLS)
A PostgreSQL feature that restricts which rows a database user can see/modify. We use it to enforce tenant isolation — tenant A's data is invisible to tenant B at the database level.
→ https://www.postgresql.org/docs/current/ddl-rowsecurity.html

### JPA + Spring Data Repositories
JPA (Java Persistence API) maps Java objects to database tables. Spring Data provides ready-made `findById`, `save`, etc. methods so you don't write SQL for every query.
→ https://spring.io/guides/gs/accessing-data-jpa/

### HikariCP
A high-performance JDBC connection pool (Spring Boot's default). Instead of opening a new DB connection per request, it reuses a pool of connections.
→ https://github.com/brettwooldridge/HikariCP

---

## S03 — Auth & Multi-Tenancy

*(entries added during S03)*

### JSON Web Tokens (JWT)
A compact, URL-safe way to represent claims (user ID, roles, tenant ID) as a signed token. The server validates the signature; no session state is needed.
→ https://jwt.io/introduction/

### OAuth2 / OIDC
OAuth2 is an authorization framework. OpenID Connect (OIDC) adds identity (who you are) on top. Keycloak implements both.
→ https://oauth.net/2/

### Keycloak
An open-source Identity and Access Management server. It handles user login, OAuth2/OIDC token issuance, and realm/tenant separation.
→ https://www.keycloak.org/documentation

### Spring Security
Spring's security framework. In this project it validates incoming JWTs, extracts claims, and enforces role-based access.
→ https://docs.spring.io/spring-security/reference/

### Multi-Tenancy
An architecture where a single application instance serves multiple "tenants" (customers/organizations) with data isolation. We implement it via Postgres RLS and a `TenantContextHolder`.
→ https://martinfowler.com/bliki/MultiTenancy.html

---

## S04 — Observability

*(entries added during S04)*

### Micrometer
A vendor-neutral metrics instrumentation library for Java. Like SLF4J for logging, but for metrics. Spring Boot auto-configures it.
→ https://micrometer.io/docs

### Prometheus
A time-series database that scrapes metrics from services at a configured interval. Stores them for querying and alerting.
→ https://prometheus.io/docs/introduction/overview/

### Grafana
A dashboarding tool that queries Prometheus (and Loki, Tempo) and renders charts. We pre-configure dashboards as JSON files committed to the repo.
→ https://grafana.com/docs/grafana/latest/

### OpenTelemetry (OTel)
A vendor-neutral instrumentation standard for distributed tracing, metrics, and logs. You add OTel spans to your code; an OTel collector forwards them to a backend.
→ https://opentelemetry.io/docs/

### Distributed Tracing
Tracking a single request as it flows across multiple services. Each hop adds a "span"; together they form a "trace". Invaluable for debugging microservices.
→ https://opentelemetry.io/docs/concepts/signals/traces/

### Loki
Grafana's log aggregation system — like Prometheus but for logs. Promtail collects logs from Docker containers and ships them to Loki.
→ https://grafana.com/docs/loki/latest/

### Tempo
Grafana's distributed tracing backend. Receives OTel spans and stores them for querying via the Grafana UI.
→ https://grafana.com/docs/tempo/latest/

---

## S05 — Schema Parser

*(entries added during S05)*

### OpenAPI 3.x
A standard machine-readable format for describing REST APIs: endpoints, parameters, request/response schemas, auth. The YAML/JSON spec.
→ https://spec.openapis.org/oas/v3.1.0

### swagger-parser
A Java library that parses and validates OpenAPI 3.x documents. Resolves `$ref` references and provides a Java object model.
→ https://github.com/swagger-api/swagger-parser

### Postman Collection v2.1
Postman's JSON format for storing API requests, environments, and test scripts. We parse it to import as an alternative to OpenAPI.
→ https://schema.postman.com/

### JSON Schema
A standard for describing the structure of JSON documents. OpenAPI uses it to describe request/response bodies.
→ https://json-schema.org/understanding-json-schema/

---

## S06 — Data Generator

*(entries added during S06)*

### Java Faker (Datafaker)
A library for generating realistic fake data: names, email addresses, phone numbers, UUIDs, etc.
→ https://www.datafaker.net/

### Property-Based Testing (JQwik)
Instead of writing specific test cases, you define *properties* ("for all valid inputs, X should hold") and the framework generates hundreds of inputs to find counterexamples.
→ https://jqwik.net/docs/current/user-guide.html

### MongoDB ObjectId
MongoDB's 24-character hexadecimal unique identifier (`^[0-9a-fA-F]{24}$`). Not a UUID. APIs backed by MongoDB often expose ObjectIds as path parameters.
→ https://www.mongodb.com/docs/manual/reference/method/ObjectId/

---

## S07 — Test Case Generator

*(entries added during S07)*

### NoSQL Injection
An attack where operators like `{"$gt": ""}` or `{"$ne": null}` are injected into query parameters to bypass MongoDB query logic. The MongoDB equivalent of SQL injection.
→ https://owasp.org/www-project-web-security-testing-guide/latest/4-Web_Application_Security_Testing/07-Input_Validation_Testing/05.6-Testing_for_NoSQL_Injection

### Idempotency
An operation is idempotent if performing it N times has the same effect as performing it once. PUT and DELETE should be idempotent by HTTP spec.
→ https://developer.mozilla.org/en-US/docs/Glossary/Idempotent

### Cursor-Based Pagination
A pagination strategy where instead of `page=2`, you use a cursor (opaque token) pointing to your place in the results. More stable under concurrent writes than offset pagination.
→ https://use-the-index-luke.com/no-offset

---

## S08 — Code Generator

*(entries added during S08)*

### FreeMarker Templates
A Java template engine for generating text files (Java code, config, HTML) from templates + data. We use it to render JUnit, Jest, k6, and Gatling test files.
→ https://freemarker.apache.org/docs/

### RestAssured
A Java DSL for writing HTTP integration tests in a given/when/then style. Works with JUnit 5.
→ https://rest-assured.io/

### Supertest
A Node.js library for testing HTTP servers. Works by sending actual HTTP requests and asserting on responses. Pairs with Jest.
→ https://github.com/ladjs/supertest

### k6
A modern load testing tool. Tests are written in JavaScript; the executor is Go. Supports VUs (virtual users), ramp-up, and output to various backends.
→ https://grafana.com/docs/k6/latest/

### Gatling
A JVM-based load testing tool with a Scala/Java DSL. We use the Java DSL in this project.
→ https://gatling.io/docs/gatling/

---

## S09 — Kafka

*(entries added during S09)*

### Apache Kafka
A distributed event streaming platform. Producers write events to topics; consumers read them. Kafka is durable — events are stored and can be replayed.
→ https://kafka.apache.org/documentation/

### KRaft Mode
Kafka's built-in consensus mechanism (replaces Zookeeper). Simplifies single-node deployments.
→ https://kafka.apache.org/documentation/#kraft

### Avro + Schema Registry
Avro is a binary serialization format for Kafka messages. The Schema Registry stores schemas and ensures producers + consumers agree on message structure.
→ https://docs.confluent.io/platform/current/schema-registry/

---

## S10 — Executor

*(entries added during S10)*

### Reactive Programming (Project Reactor / WebFlux)
A programming model for asynchronous, non-blocking I/O. Instead of blocking threads while waiting for HTTP responses, you declare data pipelines.
→ https://projectreactor.io/docs/core/release/reference/

### Spring WebClient
Spring's reactive HTTP client. Non-blocking, supports thousands of concurrent requests on a small thread pool.
→ https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html

### Java 21 Virtual Threads
Lightweight threads managed by the JVM, not the OS. You can create millions of them. We use them for the load tester's concurrent virtual users.
→ https://openjdk.org/jeps/444

### JSONPath
A query syntax for navigating and extracting values from JSON documents. Similar to XPath for XML. We use it for response variable extraction.
→ https://goessner.net/articles/JsonPath/

---

## S12 — Load Tester

*(entries added during S12)*

### TimescaleDB Hypertable
A special table type in TimescaleDB that automatically partitions data by time. Perfect for storing time-series metrics like latency samples.
→ https://docs.timescale.com/use-timescale/latest/hypertables/

### Percentile Latency (p50/p95/p99)
The latency value below which 50%/95%/99% of requests fall. p99 tells you what your worst users experience. p50 is the median.
→ https://latency.substack.com/p/percentile-latency

### Server-Sent Events (SSE)
A unidirectional streaming protocol where the server pushes events to the client over a persistent HTTP connection. Simpler than WebSockets for one-way streaming.
→ https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events

---

## S11 — Validator Module

*(entries added during S11)*

### JSON Schema Validation
Validates an actual JSON response body against the schema declared in the OpenAPI spec. Uses the everit-json-schema library, which supports JSON Schema Draft-7 including `required`, `type`, `format`, `minLength`, `enum`, etc.
→ https://github.com/erosb/everit-json-schema

### Response-Time SLA
A Service Level Agreement expressed as a maximum acceptable response time. We let API spec authors annotate endpoints with `x-response-time-sla: 500` (ms). Tests that exceed this threshold are marked as failures.
→ https://spec.openapis.org/oas/v3.1.0#specification-extensions

---

## S13 — Reporter Module

*(entries added during S13)*

### JUnit XML Format
A de-facto standard XML format for test results, supported by Jenkins, GitHub Actions, CircleCI, and most CI platforms. Each `<testsuite>` contains `<testcase>` elements; failures are nested `<failure>` elements.
→ https://www.ibm.com/docs/en/developer-for-zos/14.1?topic=formats-junit-xml-format

### Strategy Pattern
A design pattern where a family of algorithms (here: report formats) is defined behind a common interface, selected at runtime. Our `ReportRenderer` interface is implemented by `HtmlReportRenderer`, `JsonReportRenderer`, and `JUnitXmlRenderer`. The `ReporterService` picks the right one based on `ReportFormat`.
→ https://refactoring.guru/design-patterns/strategy

---

## S14 — REST API Layer

*(entries added during S14)*

### RFC 7807 Problem Details
A standard JSON format for HTTP error responses: `{ "type": "...", "title": "...", "status": 400, "detail": "..." }`. Spring 6 supports it natively via `ProblemDetail`.
→ https://datatracker.ietf.org/doc/html/rfc7807

### SpringDoc OpenAPI
A Spring Boot library that auto-generates an OpenAPI 3.x spec from your controllers via annotations. Our own API is described by its own spec (dogfooding).
→ https://springdoc.org/

---

## S16 — Frontend Foundation

*(entries added during S16)*

### React + Vite
React is the UI component library. Vite is the build tool / dev server — much faster than Webpack for development.
→ https://react.dev/ · https://vitejs.dev/guide/

### TanStack Query (React Query)
Manages server state in React: fetching, caching, synchronizing, and updating data from APIs. Replaces manual `useEffect` + `useState` for API calls.
→ https://tanstack.com/query/latest/docs/framework/react/overview

### Zustand
A minimal client-state management library for React. Simpler than Redux for the local UI state we need (selected project, current user, etc.).
→ https://docs.pmnd.rs/zustand/getting-started/introduction

### OIDC / PKCE
Authorization Code flow with Proof Key for Code Exchange. The secure way to do OAuth2 login in a browser-based app (no client secret stored in JS).
→ https://auth0.com/docs/get-started/authentication-and-authorization-flow/authorization-code-flow-with-pkce

---

## S17 — Frontend: Spec Management

*(entries added during S17)*

### Monaco Editor
The code editor from VS Code, available as a React component. We embed it for in-browser OpenAPI spec editing with syntax highlighting and validation.
→ https://microsoft.github.io/monaco-editor/

---

## S18 — Test Execution UI

*(entries added during S18)*

### EventSource (Browser SSE API)
The browser-native API for consuming Server-Sent Events. Unlike `fetch`, it automatically reconnects on failure. We use it to stream live test-run progress events from the backend's Redis pub/sub channel.
→ https://developer.mozilla.org/en-US/docs/Web/API/EventSource

### Zustand `persist` Middleware
Zustand middleware that serializes store state to `localStorage` (or `sessionStorage`) and rehydrates it on page load. We use it in `projectStore` so projects survive browser refreshes.
→ https://docs.pmnd.rs/zustand/integrations/persisting-store-data

---

## S19 — Reports & Visualization

*(entries added during S19)*

### Recharts
A React charting library built on SVG. We use it for pass/fail summaries and category breakdowns.
→ https://recharts.org/

### Apache ECharts
A more powerful charting library (canvas-based) for complex visualizations like latency distributions and percentile graphs.
→ https://echarts.apache.org/en/index.html

---

## S20 — Sample Buggy APIs

*(entries added during S20)*

### NoSQL Injection
An attack where MongoDB query operators (`$gt`, `$ne`, `$where`, `$regex`) are injected directly into query parameters or request bodies. For example, sending `username={"$gt":""}` matches all users because every string is greater than the empty string. Prevention: always sanitize/validate operator-shaped input before passing to `findOne`.
→ https://owasp.org/www-project-web-security-testing-guide/latest/4-Web_Application_Security_Testing/07-Input_Validation_Testing/05.6-Testing_for_NoSQL_Injection

### Mongoose ValidationError vs 500
When a required field is missing from a Mongoose document and `.save()` is called, Mongoose throws a `ValidationError`. If the route handler doesn't catch it, Express converts it to an unhandled 500. The fix is a `try/catch` that maps `ValidationError` to a 400 response.
→ https://mongoosejs.com/docs/validation.html

### Null-Safety in Delete-Then-Read Patterns
A common bug: `findById(id)` returns `null` on the second delete (the document no longer exists), then calling `.deleteOne()` on `null` throws a `TypeError`. The fix is always to check for `null` before dereferencing, or use `findByIdAndDelete` which handles the absent-document case gracefully.

### Express Body Size Limits
By default, `express.json()` accepts up to 100 KB. To reject oversized payloads with a proper 413 response, pass `{ limit: '100kb' }` explicitly: `app.use(express.json({ limit: '100kb' }))`.
→ https://expressjs.com/en/api.html#express.json

---

## S21 — End-to-End Integration

*(entries added during S21)*

### MockWebServer (OkHttp3)
A scriptable mock HTTP server for JVM tests. You enqueue responses or set a `Dispatcher` that returns different responses based on the request path. Invaluable for testing code that makes outbound HTTP calls (like spec fetching and API execution) without running real servers.
→ https://github.com/square/okhttp/tree/master/mockwebserver

### Pipeline E2E Testing Without Spring Context
Instead of starting the full `@SpringBootTest` application context (which needs Postgres, Redis, Kafka), you can instantiate service objects directly in a plain JUnit 5 test — as long as they have no-arg or all-final-field constructors (Lombok `@RequiredArgsConstructor`). This tests the business logic pipeline in isolation, runs in seconds, and doesn't need Docker. The trade-off: you don't test Spring auto-wiring or bean lifecycle.

### Postman Collections as Living Documentation
A Postman collection (v2.1 JSON) serves as both manual testing tool and documentation. With the `event` > `test` script blocks, each request can assert on the response — making the collection runnable as an automated test suite via Newman (`npx newman run collection.json`). Committed to the repo, it becomes a contract that future contributors can verify.
→ https://learning.postman.com/docs/collections/using-newman-cli/command-line-integration-with-newman/

---

*This file grows as the project progresses. Entries are added the first time a concept appears.*
