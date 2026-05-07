package io.github.shazaanashraff.apiforge.modules.schemaparser;

/**
 * A single endpoint parameter (path, query, header, or cookie).
 *
 * @param name parameter name as declared in the spec
 * @param in location: "path", "query", "header", or "cookie"
 * @param required true for path params (always required) and explicitly required query params
 * @param schemaType the JSON Schema type string, e.g. "string", "integer", "array"
 * @param schemaFormat the JSON Schema format hint, e.g. "uuid", "int64", "date-time"; may be null
 * @param description human-readable description from the spec
 */
public record Parameter(
    String name,
    String in,
    boolean required,
    String schemaType,
    String schemaFormat,
    String description) {}
