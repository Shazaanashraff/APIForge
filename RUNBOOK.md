# Runbook — APIForge

> Day-to-day guide for running, verifying, and troubleshooting the local APIForge stack.

---

## Quick Start

### Daily Dev (deps in Docker, code on host)

```powershell
# Start just the infrastructure dependencies
docker-compose -f docker-compose.lite.yml up -d

# Terminal 1 — backend
cd S:\apiforge\backend
.\mvnw.cmd spring-boot:run

# Terminal 2 — frontend
cd S:\apiforge\frontend
npm run dev
```

### Full Stack (everything in Docker)

```powershell
cd S:\apiforge
.\scripts\start-all.ps1
# or manually:
docker-compose up -d
```

### Stop Everything

```powershell
.\scripts\stop-all.ps1
# or manually:
docker-compose down
```

---

## Service Health Check URLs

Visit each URL to verify the service is up after startup.

| Service | URL | Expected Response |
|---|---|---|
| **APIForge Backend** | http://localhost:8081/actuator/health | `{"status":"UP"}` |
| **APIForge Frontend** | http://localhost:5173 | React app loads |
| **Java Sample API** | http://localhost:8090/actuator/health | `{"status":"UP"}` |
| **Java Sample API — OpenAPI** | http://localhost:8090/v3/api-docs | JSON spec |
| **Node Sample API** | http://localhost:3000/health | `{"status":"ok"}` |
| **Node Sample API — OpenAPI** | http://localhost:3000/api-docs | Swagger UI |
| **Keycloak** | http://localhost:8080 | Keycloak login page |
| **Grafana** | http://localhost:3001 | Grafana login page |
| **Prometheus** | http://localhost:9090 | Prometheus UI |
| **Tempo (traces UI)** | http://localhost:3200 | Tempo status |
| **Loki** | http://localhost:3100/ready | `ready` |
| **Kafka broker** | (no UI — use CLI below) | — |
| **Schema Registry** | http://localhost:8085/subjects | `[]` or list of subjects |

### Postgres Connection
```powershell
# Using psql from the Docker container
docker exec -it apiforge-postgres psql -U apiforge -d apiforge_db
# Or connection string for IntelliJ DataGrip / DBeaver:
# Host: localhost  Port: 5432  DB: apiforge_db  User: apiforge  Password: (from .env)
```

### MongoDB Connection
```powershell
# mongosh via Docker
docker exec -it apiforge-mongodb mongosh "mongodb://apiforge:apiforge_secret@localhost:27017/apiforge_samples"
# Or use MongoDB Compass with the same connection string
```

### Redis
```powershell
docker exec -it apiforge-redis redis-cli ping
# Expected: PONG
```

### Kafka — Check Topics Exist
```powershell
docker exec -it apiforge-kafka kafka-topics --bootstrap-server localhost:9092 --list
# Expected: list of topics including test-run-events, test-case-results, load-metrics
```

---

## Per-Service Startup Commands

Run individual groups when you only need part of the stack.

### Databases only (Postgres + Redis + MongoDB)
```powershell
docker-compose up -d postgres redis mongodb
```

### Observability stack only
```powershell
docker-compose up -d prometheus grafana loki promtail tempo
```

### Kafka only
```powershell
docker-compose up -d kafka schema-registry
```

### Keycloak only
```powershell
docker-compose up -d keycloak
```

### Java Sample API only
```powershell
docker-compose up -d sample-target-api-java
# Or run on host:
cd S:\apiforge\sample-target-api-java
.\mvnw.cmd spring-boot:run
```

### Node.js Sample API only
```powershell
docker-compose up -d sample-target-api-node
# Or run on host:
cd S:\apiforge\sample-target-api-node
npm run dev
```

---

## Smoke Tests

Run the full smoke test suite (takes ~2 minutes):

```powershell
.\scripts\run-smoke-tests.ps1
```

What it tests:
1. All Docker services show "healthy"
2. APIForge backend `/actuator/health` returns UP
3. Java sample API health endpoint responds
4. Node sample API health endpoint responds
5. Postgres accepts a connection and the schema version is current
6. Redis responds to PING
7. MongoDB responds to a count query

A section is NOT complete until smoke tests pass.

---

## System Verification Script

```powershell
.\scripts\verify-system.ps1
```

More thorough than smoke tests — checks:
- Docker service health for all 13+ services
- Backend Spring Actuator metrics endpoint
- Prometheus is scraping the backend
- Loki is receiving logs
- Keycloak realm is configured

Run this at the start of every session.

---

## Viewing Logs

### Per-service logs (Docker)
```powershell
docker logs apiforge-backend --tail 100 -f
docker logs apiforge-postgres --tail 50
docker logs apiforge-kafka --tail 50
```

### Aggregated logs in Grafana (Loki)
1. Open http://localhost:3001
2. Navigate to Explore → select Loki datasource
3. Query: `{container="apiforge-backend"}` or `{level="ERROR"}`

### Backend logs on host (when running via mvnw)
Logs print to stdout. Structured JSON format — pipe through `jq` for readability:
```powershell
.\mvnw.cmd spring-boot:run 2>&1 | jq .
```

---

## Viewing Traces

1. Open Grafana: http://localhost:3001
2. Navigate to Explore → select Tempo datasource
3. Search by trace ID (shown in backend logs as `traceId`) or by service name

---

## Viewing Metrics

1. Open Grafana: http://localhost:3001
2. Navigate to Dashboards → APIForge folder
3. Available dashboards (after S04):
   - **APIForge Overview** — request rate, error rate, latency
   - **Load Test Metrics** — p50/p95/p99 latency over time
   - **JVM Metrics** — heap, GC, thread counts

### Prometheus direct queries
http://localhost:9090
- `rate(http_server_requests_seconds_count[5m])` — request rate
- `http_server_requests_seconds_bucket` — latency histogram

---

## Database Operations

### Reset Postgres (wipe and re-migrate)
```powershell
.\scripts\reset-db.ps1
```
This drops and recreates the `apiforge_db` database and reruns all Flyway migrations.

### Reset MongoDB
```powershell
docker exec -it apiforge-mongodb mongosh --eval "db.getSiblingDB('apiforge_samples').dropDatabase()"
.\scripts\seed-mongo.ps1
```

### Run Flyway migrations manually
```powershell
cd S:\apiforge\backend
.\mvnw.cmd flyway:migrate
```

### View current schema version
```powershell
cd S:\apiforge\backend
.\mvnw.cmd flyway:info
```

---

## Keycloak Operations

### Reset Keycloak Realm
```powershell
.\scripts\seed-keycloak.ps1
```
This re-imports the realm configuration from `observability/keycloak/realm-export.json`.

### Keycloak Admin UI
http://localhost:8080 → log in with `admin` / `admin` (from `.env`)

### Get a test JWT token (for manual API testing)
```powershell
# Replace values from .env
$response = Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/realms/apiforge/protocol/openid-connect/token" `
  -ContentType "application/x-www-form-urlencoded" `
  -Body "grant_type=password&client_id=apiforge-backend&username=demo@apiforge.dev&password=demo123&scope=openid"
$token = $response.access_token
Write-Output $token
```

---

## Running Tests

### Backend unit tests
```powershell
cd S:\apiforge\backend
.\mvnw.cmd test
```

### Backend integration tests (requires Docker — uses Testcontainers)
```powershell
cd S:\apiforge\backend
.\mvnw.cmd verify -Pintegration
```

### Frontend tests
```powershell
cd S:\apiforge\frontend
npm test
```

### Node sample API tests
```powershell
cd S:\apiforge\sample-target-api-node
npm test
```

---

## Testing Against Sample APIs

### Test APIForge against the Java Sample API (Spring Boot + PostgreSQL)

1. Ensure the Java sample API is running: http://localhost:8090/actuator/health
2. In the APIForge frontend (http://localhost:5173):
   - Create a new project
   - Import spec from URL: `http://localhost:8090/v3/api-docs`
   - Select test categories and run
3. The sample API contains deliberate bugs (see `sample-target-api-java/README.md`)
4. The report should show failures matching the documented bugs

### Test APIForge against the Node.js Sample API (Express + MongoDB)

1. Ensure the Node sample API is running: http://localhost:3000/health
2. In the APIForge frontend:
   - Create a new project
   - Import spec from URL: `http://localhost:3000/api-docs/json`
   - **Enable the "MongoDB-backed API" toggle** in project settings
   - Select all 11 test categories and run
3. The sample API contains MongoDB-specific bugs (see `sample-target-api-node/README.md`)

### Test APIForge against Petstore

1. No running server needed — use the bundled spec file
2. In the APIForge frontend:
   - Create a new project
   - Upload spec file: `examples/petstore-openapi.yaml`
   - Run happy path + boundary tests (no auth token available for Petstore)

### Import a Postman Collection

1. In the APIForge frontend:
   - Create a new project
   - Switch input to "Postman Collection"
   - Upload: `examples/sample-postman-collection.json`
   - Review the imported endpoints and run

---

## Common Errors & Fixes

### "Address already in use" (port conflict)

```powershell
# Find what's using a port (e.g. 5432)
netstat -ano | findstr :5432
# Kill by PID
taskkill /PID <PID> /F
```

Common conflicts:
- Port 5432: local PostgreSQL installation — stop it via Services or uninstall
- Port 27017: local MongoDB installation — stop it via Services
- Port 8080: another Spring Boot app — check running apps

### Docker Desktop not starting

- Check WSL2 is enabled: `wsl --status` in an admin PowerShell
- Restart Docker Desktop via the system tray

### Backend won't connect to Postgres

- Verify Postgres container is healthy: `docker-compose ps`
- Check `.env` values match the Postgres container config
- Try connecting manually: `docker exec -it apiforge-postgres psql -U apiforge -d apiforge_db`

### Kafka consumer not receiving events

- Check Kafka is running: `docker-compose ps kafka`
- Check topics exist: `docker exec -it apiforge-kafka kafka-topics --bootstrap-server localhost:9092 --list`
- Re-run topic creation: `bash kafka/topics.sh` (or `wsl bash kafka/topics.sh` on Windows)

### Keycloak realm not imported

- Check the realm file exists: `ls observability/keycloak/realm-export.json`
- Re-run seed: `.\scripts\seed-keycloak.ps1`

### "mvnw.cmd is not recognized"

- Ensure you're in the `S:\apiforge\backend\` directory
- On first run, Maven Wrapper downloads Maven — needs internet access

### Docker memory pressure (containers crashing)

- Switch to lite stack: `docker-compose -f docker-compose.lite.yml up -d`
- Increase Docker Desktop memory allocation (Settings → Resources → Memory)

---

## Port Reference

| Port | Service |
|---|---|
| 5432 | PostgreSQL |
| 6379 | Redis |
| 27017 | MongoDB |
| 9092 | Kafka broker |
| 8085 | Schema Registry |
| 8080 | Keycloak |
| 8081 | APIForge Backend |
| 5173 | APIForge Frontend (dev) |
| 8090 | Java Sample API |
| 3000 | Node.js Sample API |
| 9090 | Prometheus |
| 3001 | Grafana |
| 3100 | Loki |
| 3200 | Tempo |
