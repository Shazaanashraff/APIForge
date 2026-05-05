# S07 — Test Case Generator Module

**Status:** ⬜ Pending
**Complexity:** XL
**Depends on:** S05, S06

## Goal

Build the 11 test category generators. Each produces a list of `TestCase` objects (structured, framework-agnostic) from an `Endpoint`. S08 renders these into actual code.

## Test Case Model

```java
public record TestCase(
    String id,               // UUID
    TestCategory category,   // one of 11 enum values
    String endpointPath,
    HttpMethod method,
    Map<String, String> headers,
    Map<String, String> queryParams,
    Map<String, String> pathParams,
    Object requestBody,
    List<Assertion> expectedAssertions,  // status code, schema, headers, SLA
    String description,
    boolean applicableIfMongoBacked      // true for MongoDB-specific tests
) {}

public enum TestCategory {
    HAPPY_PATH, BOUNDARY, NEGATIVE, AUTH, SECURITY,
    IDEMPOTENCY, RATE_LIMIT, PERFORMANCE_SLA,
    PAYLOAD_SIZE, PAGINATION, MONGODB_SPECIFIC
}
```

## Step-by-Step Tasks

### CP1 — Generator infrastructure
- [ ] `TestCaseGenerator.java` — abstract base class / interface
- [ ] `TestCaseGeneratorRegistry.java` — holds all 11 generators, dispatches by category
- [ ] `TestGenerationContext.java` — holds `Endpoint`, `DataGenerator`, project config

### CP2 — Core generators (1 per checkpoint ideally)
- [ ] `HappyPathGenerator` — valid input, expect 2xx, validate response schema
- [ ] `BoundaryGenerator` — uses `DataGenerator.generateBoundaryValues()` for each param
- [ ] `NegativeGenerator` — wrong types, missing required fields, null where required, invalid enum values
- [ ] `AuthGenerator` — 3 test cases per secured endpoint: no token (→401), expired token (→401), wrong-scope token (→403)
- [ ] `SecurityGenerator` — SQL injection payloads, XSS strings, path traversal (`../`), NoSQL operators (`{"$gt":""}`)
- [ ] `IdempotencyGenerator` — PUT/DELETE only: run twice, assert second result ≡ first
- [ ] `RateLimitGenerator` — burst N requests (from `apiforge.test-generator.rate-limit-burst-count`), assert at least one → 429
- [ ] `PerformanceSlaGenerator` — single request, assert `responseTimeMs < slaThreshold`

### CP3 — New generators
- [ ] `PayloadSizeGenerator`:
  - For POST/PUT with body: empty body, just-under-max (if declared), just-over-max (→413/400), 10× max
  - If no max declared: test 1MB, 10MB, 100MB
- [ ] `PaginationGenerator` (per pagination style detected in `Endpoint.paginationHint`):
  - Offset/limit: first page, empty page (offset > count), negative offset (→400 or default), limit=0, limit=10000
  - Page/size: first page, last page, page beyond data, negative page, size=10000
  - Cursor: first page, use returned cursor for next page, invalid cursor string
  - All styles: validate presence of `total`, `hasNext`, `nextCursor` etc. if declared in response schema
- [ ] `MongoSpecificGenerator`:
  - ObjectId format tests: `"123"`, `"not-an-id"`, 23-char hex, 25-char hex, valid-UUID-not-ObjectId
  - NoSQL injection in path param: `{$ne:null}` as path segment
  - NoSQL injection in query param: `?field[$ne]=value`, `?field[$gt]=`
  - Condition: `TestCase.applicableIfMongoBacked = true` unless project flag is set

### CP4 — MongoDB detection
- [ ] `MongoBackedApiDetector.java`:
  - Check project `isMongoBackedApi` flag first (user-explicit)
  - Heuristic: if any response example has IDs matching `^[0-9a-fA-F]{24}$` → likely Mongo
  - If uncertain: generate tests but mark `applicableIfMongoBacked = true`

### CP5 — Unit tests
- [ ] `HappyPathGeneratorTest` — Petstore `GET /pets/{id}` → expect 1 test case with GET + valid ID
- [ ] `PaginationGeneratorTest` — offset/limit endpoint → expect 6+ test cases
- [ ] `PayloadSizeGeneratorTest` — POST endpoint with `maxLength: 1000` → expect 4 test cases
- [ ] `MongoSpecificGeneratorTest` — endpoint with ObjectId path param → expect 7+ test cases
- [ ] `AuthGeneratorTest` — secured endpoint → expect exactly 3 test cases
- [ ] `SecurityGeneratorTest` — any POST → contains NoSQL injection cases

## Files to Create/Modify

```
backend/src/main/java/.../modules/testgenerator/
  TestCase.java  TestCategory.java  Assertion.java
  TestCaseGenerator.java  TestCaseGeneratorRegistry.java  TestGenerationContext.java
  HappyPathGenerator.java  BoundaryGenerator.java  NegativeGenerator.java
  AuthGenerator.java  SecurityGenerator.java  IdempotencyGenerator.java
  RateLimitGenerator.java  PerformanceSlaGenerator.java
  PayloadSizeGenerator.java  PaginationGenerator.java  MongoSpecificGenerator.java
  MongoBackedApiDetector.java
  SecurityPayloads.java  (constants: SQL injection strings, XSS strings, NoSQL operators)
backend/src/test/java/.../modules/testgenerator/
  HappyPathGeneratorTest.java  PaginationGeneratorTest.java
  PayloadSizeGeneratorTest.java  MongoSpecificGeneratorTest.java
  AuthGeneratorTest.java  SecurityGeneratorTest.java
```

## Manual Verification

After S14 wires this up:
```
POST /api/projects/{id}/generate?categories=ALL
→ Returns test case count grouped by category
→ For Petstore: expect > 100 cases across 11 categories
→ For Node sample (Mongo flag on): expect MongoDB-specific tests in the response
```

## Definition of Done

- [ ] All 6 test classes pass
- [ ] Petstore generates ≥80 test cases across ≥8 categories
- [ ] Node sample (Mongo flag) generates ≥10 MongoDB-specific cases
- [ ] PROGRESS.md updated
