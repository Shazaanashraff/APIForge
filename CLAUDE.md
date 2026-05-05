# CLAUDE.md — APIForge Agent Instructions

> Read this file at the START of every session before touching any code.

---

## 1. Project Identity

- **Name:** APIForge
- **Repo:** https://github.com/Shazaanashraff/APIForge.git
- **Root:** `S:\apiforge\`
- **Java package root:** `io.github.shazaanashraff.apiforge`
- **Java version:** 21 (virtual threads enabled via `spring.threads.virtual.enabled=true`)
- **Spring Boot:** 3.x
- **Node version:** 20+

---

## 2. Resume Protocol (READ FIRST EVERY SESSION)

1. Read this file (`CLAUDE.md`)
2. Read `PROGRESS.md` — resume from the last checkpoint exactly
3. Read the current section's plan: `docs/plans/SXX-*.md`
4. Run `git log --oneline -10` to see recent commits
5. Run `git status` to check working tree
6. Run `.\scripts\verify-system.ps1` to confirm system state
7. Confirm: "Resuming from [checkpoint ID]: [what's next]. Proceed?"
8. Wait for user confirmation

---

## 3. Naming Conventions

### Java
- Packages: `io.github.shazaanashraff.apiforge.<module>.<subpackage>` (all lowercase)
- Classes: `PascalCase`
- Methods/fields: `camelCase`
- Constants: `SCREAMING_SNAKE_CASE`
- Test classes: `<Subject>Test` (unit), `<Subject>IntegrationTest` (integration)
- Spring beans: named by role, not impl (e.g. `schemaParser`, not `swaggerSchemaParserImpl`)

### TypeScript/JavaScript
- Components: `PascalCase.tsx`
- Hooks: `use<Name>.ts`
- Utilities: `camelCase.ts`
- API clients: `<resource>Api.ts`
- Stores: `use<Name>Store.ts`

### Database
- Tables: `snake_case` plural (e.g. `test_runs`)
- Columns: `snake_case`
- Flyway migrations: `V{N}__{description_with_underscores}.sql`
- All tenant-scoped tables must have `tenant_id UUID NOT NULL` column

---

## 4. Module Layout

All Spring modules live under `backend/src/main/java/io/github/shazaanashraff/apiforge/modules/`:

| Module | Package | Purpose |
|---|---|---|
| `schemaparser` | `modules.schemaparser` | OpenAPI + Postman → internal model |
| `testgenerator` | `modules.testgenerator` | 11 test category generators |
| `datagenerator` | `modules.datagenerator` | Faker + JQwik + ObjectId |
| `codegenerator` | `modules.codegenerator` | JUnit/Jest/k6/Gatling template renderer |
| `executor` | `modules.executor` | Reactive WebClient + chaining + auth |
| `validator` | `modules.validator` | Response validation (status/schema/SLA) |
| `loadtester` | `modules.loadtester` | Virtual threads + load scenarios |
| `reporter` | `modules.reporter` | HTML/JSON/JUnit XML report builder |
| `project` | `modules.project` | Project and configuration management |
| `auth` | `modules.auth` | JWT + Keycloak integration |
| `tenancy` | `modules.tenancy` | Tenant context, RLS propagation |
| `shared` | `shared` | Common utilities, config, base types |

Modules must NOT import each other directly. Use Spring Modulith's `@ApplicationModule` events or public API interfaces for cross-module calls.

---

## 5. Always Do

- Use **virtual threads** for executor tasks: `spring.threads.virtual.enabled=true` (already configured)
- Add **Micrometer metrics** to every new module (at minimum a counter and a timer)
- Add **OpenTelemetry span** around every significant operation (S04+)
- Use **structured JSON logging** — never `System.out.println`; always `log.info("message", kv("key", val))` with Logback structured logging
- Add **Flyway migration** for every schema change — never modify existing migration files
- Set **`tenant_id`** on every tenant-scoped entity creation
- Use **`@Transactional`** on service methods that write to the database
- Use **RFC 7807 `ProblemDetail`** for all error responses (Spring 6 built-in)
- Keep code **readable over clever** — this is a teaching codebase
- Write **one comment per non-obvious WHY** (not what)

---

## 6. Never Do

- Never call `System.out.println` — use SLF4J logger
- Never modify an existing Flyway migration file — always add a new one
- Never store secrets in committed files — use `.env` loaded via Docker Compose / `application.yml` property placeholders
- Never bypass Spring Modulith module boundaries — use events or public API interfaces
- Never use `Thread.sleep()` in production code — use reactive operators or `ScheduledExecutorService`
- Never expose raw stack traces in HTTP responses
- Never commit with `wip:` prefix to `main` — use a feature branch
- Never skip the Definition of Done (all 5 items required per checkpoint)
- Never add `@SuppressWarnings` without a comment explaining why

---

## 7. Definition of Done (per checkpoint — ALL 5 required)

- [ ] Code written and compiles / runs without errors
- [ ] Tests pass (`mvnw test` or `npm test`)
- [ ] Docs updated (section README, inline comments if new concept, ADR if architectural decision)
- [ ] Committed with Conventional Commits format
- [ ] `PROGRESS.md` updated with new checkpoint state

A checkpoint is **NOT complete** until all 5 are checked.

---

## 8. Commit Format (Conventional Commits)

```
<type>(<scope>): <subject>

[optional body]
```

Types: `feat` | `fix` | `docs` | `style` | `refactor` | `test` | `chore` | `perf` | `ci` | `build`

Scope: module name or file group (e.g. `test-gen`, `executor`, `frontend`, `docker`, `ci`)

Examples:
- `feat(test-gen): add pagination test generator with cursor-style detection`
- `chore(backend): add Maven project skeleton with all module packages`
- `docs(plans): add per-section implementation plans S01-S23`
- `fix(executor): handle 401 on first request before auth refresh loop`

Prefix `wip:` ONLY on non-main branches when saving incomplete state for context-budget reasons.

---

## 9. Token Budget Protocol

1. **Start of session**: read CLAUDE.md + PROGRESS.md immediately (before any tool calls)
2. **Every ~10 tool calls**: self-assess remaining context
3. **At ~70% context used**: finish current sub-task, don't start anything new
4. **At ~85% context used**: STOP. Even if mid-feature:
   - Commit working code (`wip:` prefix on feature branch if incomplete)
   - Update `PROGRESS.md` with exact file, line, and mental state
   - Tell the user: "Context limit approaching. State saved to PROGRESS.md. Start a new session and I'll resume from S0X-CPY."
5. Never lose work to context overflow.

---

## 10. Error & Logging Standards

```java
// Use module-level logger
private static final Logger log = LoggerFactory.getLogger(MyClass.class);

// Structured key-value logging (Logback with logstash-logback-encoder)
log.info("Test run started", kv("runId", runId), kv("tenantId", tenantId));
log.error("Schema parse failed", kv("url", specUrl), e);
```

HTTP error responses — always use Spring 6 ProblemDetail:
```java
ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Schema URL is unreachable");
problem.setProperty("url", specUrl);
return ResponseEntity.badRequest().body(problem);
```

---

## 11. Tech Stack Quick Reference

| Layer | Technology | Version |
|---|---|---|
| Java | OpenJDK | 21 |
| Spring Boot | Spring Boot | 3.x |
| Build | Maven | 3.9+ (mvnw.cmd) |
| DB | PostgreSQL + TimescaleDB | 16 |
| Cache | Redis | 7 |
| Messaging | Kafka (KRaft) | latest Confluent |
| Auth | Keycloak | latest |
| Frontend | React + Vite + TS | 18 / 5 / 5 |
| Styling | Tailwind CSS | 3.x |
| State | Zustand + TanStack Query | latest |
| Charts | Recharts + Apache ECharts | latest |
| Editor | Monaco Editor | latest |
| Node sample | Node.js + Express + Mongoose | 20+ / 4.x |
| Container | Docker Desktop + WSL2 | - |

---

## 12. Key File Locations

| File | Purpose |
|---|---|
| `PROGRESS.md` | The resume file — always up-to-date |
| `docs/MASTER_PLAN.md` | Top-level section roadmap |
| `docs/plans/SXX-*.md` | Per-section detailed plans |
| `docs/adr/*.md` | Architecture Decision Records |
| `.env.example` | Environment variable template |
| `docker-compose.yml` | Full local stack |
| `docker-compose.lite.yml` | Deps-only (postgres + redis + mongo) |
| `scripts/verify-system.ps1` | Quick health check script |
| `LEARNING.md` | Concepts index + external links |

---

## 13. Windows / PowerShell Notes

- All scripts are `.ps1` — run with `.\scripts\<name>.ps1`
- Use `S:\apiforge\` as the working directory
- Maven wrapper: `.\mvnw.cmd` (not `./mvnw`)
- Docker compose: `docker-compose` (v1 syntax, for compatibility)
- File paths in configs use `/` (forward slashes) even on Windows inside Docker containers
- WSL2 backend required for Docker Desktop

---

*Last updated: 2026-05-05 — S01-CP1*
