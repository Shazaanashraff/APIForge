package io.github.shazaanashraff.apiforge.modules.schemaparser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

/**
 * Parses a Postman Collection v2.1 JSON document into the internal {@link Endpoint} model.
 *
 * <p>Only the HTTP request metadata is used (method, URL, headers, body example).
 * Pre-request scripts, test scripts, and Postman-specific variables are ignored.
 * Nested folder hierarchies are flattened — all requests regardless of folder depth are returned.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostmanParser {

  private final ObjectMapper objectMapper;
  private final PaginationHintDetector paginationHintDetector;
  private final IdFormatDetector idFormatDetector;

  /**
   * Parses a raw Postman Collection v2.1 JSON string.
   *
   * @throws SpecParseException when the content is not valid Postman v2.1 JSON
   */
  public List<Endpoint> parse(String collectionJson) {
    JsonNode root;
    try {
      root = objectMapper.readTree(collectionJson);
    } catch (Exception e) {
      throw new SpecParseException("Invalid Postman collection JSON: " + e.getMessage(), e);
    }

    JsonNode schema = root.path("info").path("schema");
    if (!schema.isMissingNode() && !schema.asText().contains("v2.1")) {
      log.warn("Postman collection schema is not v2.1 — parsing may be incomplete");
    }

    List<Endpoint> endpoints = new ArrayList<>();
    collectItems(root.path("item"), endpoints);
    log.info("Parsed {} endpoint(s) from Postman collection", endpoints.size());
    return endpoints;
  }

  private void collectItems(JsonNode items, List<Endpoint> out) {
    if (!items.isArray()) {
      return;
    }
    for (JsonNode item : items) {
      if (item.has("item")) {
        // Folder — recurse into nested items
        collectItems(item.path("item"), out);
      } else if (item.has("request")) {
        out.add(mapRequest(item));
      }
    }
  }

  private Endpoint mapRequest(JsonNode item) {
    JsonNode request = item.path("request");
    String rawMethod = request.path("method").asText("GET").toUpperCase();
    HttpMethod method = parseMethod(rawMethod);

    // Resolve URL path — strip Postman variable syntax {{var}} but keep path shape
    String rawUrl = resolveUrl(request.path("url"));
    String path = normalizePath(rawUrl);

    // Query parameters
    List<Parameter> queryParams = new ArrayList<>();
    JsonNode urlNode = request.path("url");
    if (urlNode.has("query")) {
      for (JsonNode q : urlNode.path("query")) {
        String name = q.path("key").asText();
        if (!name.isBlank()) {
          queryParams.add(new Parameter(name, "query", false, "string", null,
              q.path("description").asText(null)));
        }
      }
    }

    // Request body
    RequestBodySchema body = null;
    JsonNode bodyNode = request.path("body");
    if (!bodyNode.isMissingNode() && bodyNode.has("raw")) {
      body = new RequestBodySchema(false, "application/json", bodyNode.path("raw").asText());
    }

    // Auth from headers
    AuthRequirement auth = detectAuth(request.path("header"));

    PaginationHint pagination = paginationHintDetector.detect(queryParams);

    return new Endpoint(
        path, method,
        null, // Postman has no operationId
        item.path("name").asText(),
        queryParams, body, Map.of(),
        auth, pagination, PayloadSizeHint.unknown(), SlaHint.none(), IdFormatHint.unknown(),
        List.of());
  }

  private String resolveUrl(JsonNode urlNode) {
    if (urlNode.isTextual()) {
      return urlNode.asText();
    }
    if (urlNode.has("raw")) {
      return urlNode.path("raw").asText();
    }
    return "/";
  }

  private String normalizePath(String rawUrl) {
    // Strip protocol + host: keep only the path segment
    String url = rawUrl.replaceAll("\\{\\{[^}]+}}", ":var"); // {{baseUrl}}/foo → :var/foo
    int pathStart = url.indexOf('/', url.indexOf("://") > -1 ? url.indexOf("://") + 3 : 0);
    String path = pathStart >= 0 ? url.substring(pathStart) : "/" + url;
    // Drop query string
    int q = path.indexOf('?');
    return q >= 0 ? path.substring(0, q) : path;
  }

  private AuthRequirement detectAuth(JsonNode headers) {
    if (headers.isArray()) {
      for (JsonNode h : headers) {
        String key = h.path("key").asText().toLowerCase();
        String value = h.path("value").asText().toLowerCase();
        if ("authorization".equals(key)) {
          if (value.startsWith("bearer")) return AuthRequirement.BEARER_JWT;
          if (value.startsWith("basic")) return AuthRequirement.BASIC;
          if (value.startsWith("apikey") || value.startsWith("api-key"))
            return AuthRequirement.API_KEY;
          return AuthRequirement.BEARER_JWT;
        }
        if (key.contains("x-api-key") || key.contains("api-key")) {
          return AuthRequirement.API_KEY;
        }
      }
    }
    return AuthRequirement.NONE;
  }

  private HttpMethod parseMethod(String method) {
    try {
      return HttpMethod.valueOf(method);
    } catch (IllegalArgumentException e) {
      return HttpMethod.GET;
    }
  }
}
