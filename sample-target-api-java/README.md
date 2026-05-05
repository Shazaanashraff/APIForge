# Sample Target API — Java (Spring Boot + PostgreSQL)

> A deliberately buggy REST API. APIForge should detect all bugs listed below.

This API simulates a simple e-commerce backend: Users, Products, Orders.

## Running

```powershell
# With Docker Compose (from project root)
docker-compose up -d sample-target-api-java

# Locally (needs Postgres running)
cd sample-target-api-java
.\mvnw.cmd spring-boot:run
```

- Health: http://localhost:8090/actuator/health
- Swagger UI: http://localhost:8090/swagger-ui.html
- OpenAPI spec: http://localhost:8090/v3/api-docs

---

## Bug List (What APIForge Should Catch)

| # | Endpoint | Bug | Test Category That Catches It |
|---|---|---|---|
| B1 | `POST /products` | Returns `200 OK` instead of `201 Created` | Happy path |
| B2 | `GET /products/{id}` | Returns `500` for non-existent ID instead of `404` | Negative |
| B3 | `GET /products` | Missing `total` and `hasNext` fields in paginated response | Pagination |
| B4 | `GET /products` | Accepts `limit=99999` without capping — returns all records | Pagination |
| B5 | `POST /orders` | Accepts payloads larger than 1MB without returning `413` | Payload size |
| B6 | `GET /search` | Deliberate `Thread.sleep(3000)` — exceeds 500ms SLA | Performance SLA |
| B7 | `DELETE /products/{id}` | Not idempotent — second `DELETE` returns `500` instead of `404` or `204` | Idempotency |
| B8 | `GET /admin/users` | Accessible without a valid auth token (missing `@PreAuthorize`) | Auth |
| B9 | `GET /search?q=` | Raw JPQL concatenation — SQL injection vulnerable | Security |
| B10 | `PUT /products/{id}` | Missing required `updatedAt` field in response body | Happy path / Boundary |

---

## Intended Endpoints

```
GET    /products          — list products (paginated: ?page=0&size=20)
POST   /products          — create product
GET    /products/{id}     — get product by ID
PUT    /products/{id}     — update product
DELETE /products/{id}     — delete product
GET    /search?q=         — full-text search (buggy)
POST   /orders            — create order
GET    /orders/{id}       — get order by ID
GET    /admin/users       — list users (should require ADMIN role — buggy)
POST   /auth/login        — returns JWT token
```

---

## Implementation Status

Implementation begins in **S20** of the master plan.
This README describes the intended design and bugs so APIForge test categories can be written to detect them (S07) before the API is built.
