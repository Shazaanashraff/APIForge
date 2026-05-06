package io.github.shazaanashraff.apiforge.modules.schemaparser;

/**
 * A single declared response for an endpoint.
 *
 * @param statusCode  HTTP status code, e.g. 200 or 404; -1 for "default"
 * @param description human-readable description
 * @param schemaJson  the resolved JSON Schema of the response body; null when not declared
 */
public record ResponseSchema(int statusCode, String description, String schemaJson) {}
