# S06 — Data Generator Module

**Status:** ⬜ Pending
**Complexity:** M
**Depends on:** S05

## Goal

Build the `datagenerator` module that produces schema-aware, realistic test data. Used by every test category generator in S07.

## Key Capabilities

1. **Faker-based**: names, emails, UUIDs, numbers — from `net.datafaker:datafaker`
2. **Schema-constraint-aware**: respects `minLength`, `maxLength`, `pattern`, `enum`, `format`, `minimum`, `maximum`, `minItems`, `maxItems`
3. **JQwik property-based**: generates edge cases for boundary tests (just-under/just-over min/max)
4. **Seedable**: `DataGenerator(long seed)` constructor for deterministic test runs
5. **ObjectId generators**: valid (24-char hex), invalid-but-similar (24 chars, non-hex), wrong-length, UUID-looking

## Step-by-Step Tasks

### CP1 — Core DataGenerator
- [ ] `DataGenerator.java` — main facade:
  - `generateValidValue(JsonSchema schema)` → Object
  - `generateBoundaryValues(JsonSchema schema)` → `List<Object>` (min, max, just-under, just-over)
  - `generateInvalidValue(JsonSchema schema)` → Object (wrong type, null, etc.)
  - `generatePayload(RequestBodySchema schema)` → `Map<String, Object>` (full valid body)
  - `generateOversizedPayload(RequestBodySchema schema, int multiplier)` → `byte[]`

### CP2 — Format-specific generators
- [ ] `StringGenerator.java` — respects minLength/maxLength/pattern
- [ ] `NumberGenerator.java` — respects minimum/maximum/multipleOf
- [ ] `ArrayGenerator.java` — respects minItems/maxItems
- [ ] `EmailGenerator.java`, `UuidGenerator.java`, `DateGenerator.java`
- [ ] `MongoObjectIdGenerator.java`:
  - `validObjectId()` — `new org.bson.types.ObjectId().toHexString()` equivalent (24-char hex without MongoDB dep: just generate random 24 hex chars)
  - `invalidObjectId()` — 24 chars but non-hex characters
  - `wrongLengthObjectId()` — 23 chars or 25 chars
  - `uuidLookingObjectId()` — valid UUID format but not 24-char hex

### CP3 — JQwik integration
- [ ] `BoundaryValueProvider.java` — uses JQwik `@Provide` methods to generate boundary value lists
  - Integers: `[min, min+1, max-1, max, min-1, max+1]`
  - Strings: `["", "a" * minLength, "a" * maxLength, "a" * (maxLength+1)]`
  - Arrays: empty, minItems-1, minItems, maxItems, maxItems+1

### CP4 — Tests
- [ ] `DataGeneratorTest` — verify seedability (same seed → same output)
- [ ] `MongoObjectIdGeneratorTest` — verify valid/invalid formats
- [ ] `BoundaryValueProviderTest` — verify boundary arrays are correct

## Manual Verification

```java
// Quick smoke test in a @SpringBootTest:
DataGenerator gen = new DataGenerator(42L);
Map<String, Object> payload = gen.generatePayload(someSchema);
// Run twice with same seed → identical output
```

## Definition of Done

- [ ] All 3 test classes pass
- [ ] `DataGenerator(42L).generatePayload(petStoreNewPetSchema)` produces a valid pet JSON
- [ ] PROGRESS.md updated
