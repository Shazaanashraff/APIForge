package io.github.shazaanashraff.apiforge.modules.schemaparser;

/**
 * The request body declared for an endpoint.
 *
 * @param required whether the body is mandatory
 * @param contentType primary content-type, e.g. "application/json"
 * @param schemaJson the resolved JSON Schema as a JSON string; null when not parseable
 */
public record RequestBodySchema(boolean required, String contentType, String schemaJson) {}
