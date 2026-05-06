package io.github.shazaanashraff.apiforge.modules.schemaparser;

import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Parses OpenAPI 3.x specs (JSON or YAML) into the internal {@link Endpoint} model.
 *
 * <p>Uses swagger-parser for $ref resolution. If the parser emits validation warnings they are
 * logged at WARN level so callers can surface them in the UI.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenApiParser {

  private final EndpointMapper endpointMapper;

  /** Parses a raw spec string (JSON or YAML). */
  public List<Endpoint> parse(String specContent) {
    ParseOptions options = new ParseOptions();
    options.setResolve(true);
    options.setResolveFully(true);

    SwaggerParseResult result = new OpenAPIV3Parser().readContents(specContent, null, options);

    if (result.getMessages() != null && !result.getMessages().isEmpty()) {
      result.getMessages().forEach(msg -> log.warn("OpenAPI parse warning: {}", msg));
    }
    if (result.getOpenAPI() == null) {
      throw new SpecParseException(
          "Could not parse OpenAPI spec. Errors: " + result.getMessages());
    }
    return endpointMapper.mapAll(result.getOpenAPI());
  }

  /**
   * Parses an OpenAPI spec from a URL.
   *
   * <p>swagger-parser handles the HTTP fetch internally, so no WebClient is needed here.
   */
  public List<Endpoint> parseFromUrl(String url) {
    ParseOptions options = new ParseOptions();
    options.setResolve(true);
    options.setResolveFully(true);

    SwaggerParseResult result = new OpenAPIV3Parser().readLocation(url, null, options);

    if (result.getMessages() != null && !result.getMessages().isEmpty()) {
      result.getMessages().forEach(msg -> log.warn("OpenAPI parse warning ({}): {}", url, msg));
    }
    if (result.getOpenAPI() == null) {
      throw new SpecParseException(
          "Could not fetch/parse OpenAPI spec from " + url + ". Errors: " + result.getMessages());
    }
    return endpointMapper.mapAll(result.getOpenAPI());
  }

  /**
   * Tries standard OpenAPI discovery paths on a running server in order:
   * {@code /v3/api-docs}, {@code /openapi.json}, {@code /swagger.json}, {@code /api-docs}.
   */
  public List<Endpoint> introspect(String baseUrl) {
    String cleanBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    List<String> candidates = List.of(
        cleanBase + "/v3/api-docs",
        cleanBase + "/openapi.json",
        cleanBase + "/swagger.json",
        cleanBase + "/api-docs");

    for (String url : candidates) {
      try {
        List<Endpoint> endpoints = parseFromUrl(url);
        log.info("Introspected {} endpoint(s) from {}", endpoints.size(), url);
        return endpoints;
      } catch (SpecParseException e) {
        log.debug("Discovery failed at {}: {}", url, e.getMessage());
      }
    }
    throw new SpecParseException(
        "Could not discover OpenAPI spec at " + baseUrl + " — tried " + candidates);
  }
}
