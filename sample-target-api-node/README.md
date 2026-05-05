# Sample Target API — Node.js (Express + MongoDB)

> A deliberately buggy REST API. APIForge should detect all bugs listed below, including MongoDB-specific ones.

This API simulates a simple user/product catalog backed by MongoDB.

## Running

```powershell
# With Docker Compose (from project root)
docker-compose up -d sample-target-api-node

# Locally (needs MongoDB running)
cd sample-target-api-node
npm run dev
```

- Health: http://localhost:3000/health
- Swagger UI: http://localhost:3000/api-docs
- OpenAPI JSON: http://localhost:3000/api-docs/json

---

## Bug List (What APIForge Should Catch)

| # | Endpoint | Bug | Test Category That Catches It |
|---|---|---|---|
| B1 | `GET /users/{id}` | Returns `500` for invalid ObjectId format (e.g. `"abc"`) instead of `400` | MongoDB-specific / Negative |
| B2 | `GET /users?role` | NoSQL injection: `?role[$ne]=admin` bypasses role filter | Security (NoSQL injection) |
| B3 | `POST /users/login` | Body `{"username": {"$gt": ""}}` logs in as any user | Security (NoSQL injection) |
| B4 | `GET /products` | Off-by-one: returns `page+1` worth of items at offset `page*size` | Pagination |
| B5 | `GET /products` | Missing `total` field in paginated response | Pagination |
| B6 | `GET /search?q=` | No index on `name` field — slow query on large datasets | Performance SLA |
| B7 | `POST /products` | Mongoose `ValidationError` leaks as unhandled `500` instead of `400` | Negative |
| B8 | `DELETE /products/{id}` | Second delete returns `500` (Mongoose `DocumentNotFoundError` unhandled) | Idempotency |
| B9 | `GET /products` | Accepts `limit=100000` — no cap, returns entire collection | Pagination |
| B10 | `POST /upload` | Accepts unlimited body size — no `413` for oversized payloads | Payload size |

---

## MongoDB-Specific Bug Details

**B1 — ObjectId format validation (broken)**
```
GET /users/not-a-valid-id
→ Expected: 400 Bad Request
→ Actual:   500 Internal Server Error (CastError: Cast to ObjectId failed)
```

**B2 — NoSQL injection via query param**
```
GET /users?role[$ne]=admin
→ Expected: 400 Bad Request or only regular users
→ Actual:   Returns all non-admin users (injection successful)
```

**B3 — NoSQL injection in login body**
```
POST /users/login  { "username": {"$gt": ""}, "password": "anything" }
→ Expected: 400 Bad Request
→ Actual:   Returns a user's JWT token (authentication bypass)
```

---

## Intended Endpoints

```
GET    /health              — health check
GET    /users               — list users (?page=0&size=20&role=)
POST   /users               — create user
GET    /users/{id}          — get user by ObjectId
PUT    /users/{id}          — update user
DELETE /users/{id}          — delete user
POST   /users/login         — authenticate and get JWT
GET    /products            — list products (paginated)
POST   /products            — create product
GET    /products/{id}       — get product by ObjectId
PUT    /products/{id}       — update product
DELETE /products/{id}       — delete product
GET    /search?q=           — search products (slow — no index)
POST   /upload              — upload product image (body size bug)
```

---

## Implementation Status

Implementation begins in **S20** of the master plan.
This README documents the bugs so APIForge test generators (S07) can be written to detect them before this API is built.
