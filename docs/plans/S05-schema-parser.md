# S05 — Schema Parser Module

**Status:** ⬜ Pending
**Complexity:** L
**Depends on:** S01, S04

## Goal

Build the `schemaparser` module that accepts an OpenAPI 3.x spec (file upload, URL, or live introspection) OR a Postman v2.1 collection and produces a normalized `Endpoint[]` internal model. This model is the input to every downstream generator.

## Architecture Decisions

- **swagger-parser** as the primary OpenAPI parser (handles `$ref` resolution)
- **openapi4j** as cross-validation / fallback when swagger-parser produces warnings
- **Postman v2.1**: parse-only (no pre-request scripts)
- Internal model (`Endpoint`, `Parameter`, `RequestBodySchema`, `ResponseSchema`) decoupled from both libraries
- **Metadata enrichment**: pagination hints, ObjectId detection, SLA hints extracted from schema annotations

## Internal Model

```java
// All test generators consume this — changes here affect everything downstream
public record Endpoint(
    String path,
    HttpMethod method,
    String operationId,
    String summary,
    List<Parameter> parameters,
    RequestBodySchema requestBody,    // nullable
    Map<Integer, ResponseSchema> responses,
    AuthRequirement authRequirement,
    PaginationHint paginationHint,    // detected from query params
    PayloadSizeHint payloadSizeHint,  // from schema maxLength/maxItems
    SlaHint slaHint,                  // from x-response-time-sla extension
    IdFormatHint idFormatHint,        // UUID vs ObjectId
    List<String> tags
) {}
```

## Step-by-Step Tasks

### CP1 — Internal model classes
- [ ] `Endpoint.java`, `Parameter.java`, `RequestBodySchema.java`, `ResponseSchema.java` (records)
- [ ] `AuthRequirement.java` (enum: NONE, BEARER_JWT, API_KEY, BASIC)
- [ ] `PaginationHint.java` (style: OFFSET_LIMIT | PAGE_SIZE | CURSOR | NONE, detected param names)
- [ ] `PayloadSizeHint.java` (declaredMaxBytes, hasDeclaredMax)
- [ ] `SlaHint.java` (thresholdMs — from `x-response-time-sla` or null)
- [ ] `IdFormatHint.java` (format: UUID | OBJECTID | UNKNOWN)

### CP2 — OpenAPI 3.x parser
- [ ] `OpenApiParser.java` — wraps swagger-parser
  - `parse(String specJson)` → `List<Endpoint>`
  - `parseFromUrl(String url)` → `List<Endpoint>` (uses WebClient)
  - `introspect(String baseUrl)` → tries `/v3/api-docs`, `/swagger.json`, `/openapi.json`, `/api-docs`
  - Cross-validation: if swagger-parser emits warnings, run openapi4j and log discrepancies
- [ ] `EndpointMapper.java` — converts swagger-parser model to internal `Endpoint` records
- [ ] `PaginationHintDetector.java` — inspects query params for pagination patterns
- [ ] `IdFormatDetector.java` — regex `^[0-9a-fA-F]{24}$` + `format: objectid` check

### CP3 — Postman v2.1 parser
- [ ] `PostmanParser.java` — reads Postman Collection JSON (Jackson)
  - Maps Postman items → `Endpoint` records
  - Extracts: method, URL (with variable substitution), headers, body examples
  - Does NOT execute pre-request scripts

### CP4 — File upload + URL ingestion service
- [ ] `SpecIngestionService.java` (public API of the module):
  - `ingestFile(MultipartFile)` → `ParsedSpec`
  - `ingestUrl(String url)` → `ParsedSpec` (detects if OpenAPI or Postman by content-type / root keys)
  - `introspect(String baseUrl)` → `ParsedSpec`
- [ ] `ParsedSpec.java` — wraps `List<Endpoint>` with metadata (source URL, parsed at, endpoint count)

### CP5 — Unit tests
- [ ] `OpenApiParserTest` — parse `examples/petstore-openapi.yaml` → expect N endpoints
- [ ] `PaginationHintDetectorTest` — verify offset/page/cursor detection with various param names
- [ ] `PostmanParserTest` — parse `examples/sample-postman-collection.json`
- [ ] `IdFormatDetectorTest` — verify ObjectId vs UUID detection

## Files to Create/Modify

```
backend/src/main/java/.../modules/schemaparser/
  Endpoint.java  Parameter.java  RequestBodySchema.java  ResponseSchema.java
  AuthRequirement.java  PaginationHint.java  PayloadSizeHint.java  SlaHint.java  IdFormatHint.java
  OpenApiParser.java  EndpointMapper.java  PaginationHintDetector.java  IdFormatDetector.java
  PostmanParser.java
  SpecIngestionService.java  ParsedSpec.java
backend/src/test/java/.../modules/schemaparser/
  OpenApiParserTest.java  PaginationHintDetectorTest.java
  PostmanParserTest.java  IdFormatDetectorTest.java
examples/petstore-openapi.yaml         (add real Petstore spec)
examples/sample-postman-collection.json
```

## Manual Verification

```powershell
# After S14 exposes the REST API:
curl -F "file=@examples/petstore-openapi.yaml" http://localhost:8081/api/specs/upload
# Expected: JSON with endpoint list including pagination hints where applicable
```

## Definition of Done

- [ ] All 5 unit tests pass
- [ ] Petstore parse produces correct endpoint count
- [ ] `PaginationHintDetector` correctly identifies all 3 styles
- [ ] PROGRESS.md updated
