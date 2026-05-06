package io.github.shazaanashraff.apiforge.modules.schemaparser;

/** Authentication scheme required to call an endpoint, as detected from the spec's securitySchemes. */
public enum AuthRequirement {
  NONE,
  BEARER_JWT,
  API_KEY,
  BASIC
}
