package io.github.shazaanashraff.apiforge.modules.schemaparser;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

/**
 * Converts a swagger-parser {@link OpenAPI} model into our internal {@link Endpoint} records.
 *
 * <p>Note: local variable {@code swaggerParam} is used throughout to avoid import collision
 * with our own {@link Parameter} record.
 */
@Slf4j
@Component
@RequiredArgsConstructor
class EndpointMapper {

  private final PaginationHintDetector paginationHintDetector;
  private final IdFormatDetector idFormatDetector;
  private final ObjectMapper objectMapper;

  List<Endpoint> mapAll(OpenAPI openAPI) {
    if (openAPI.getPaths() == null) {
      return List.of();
    }

    List<Endpoint> endpoints = new ArrayList<>();
    for (Map.Entry<String, PathItem> entry : openAPI.getPaths().entrySet()) {
      String path = entry.getKey();
      PathItem pathItem = entry.getValue();

      mapOperation(path, HttpMethod.GET,    pathItem.getGet(),    openAPI, endpoints);
      mapOperation(path, HttpMethod.POST,   pathItem.getPost(),   openAPI, endpoints);
      mapOperation(path, HttpMethod.PUT,    pathItem.getPut(),    openAPI, endpoints);
      mapOperation(path, HttpMethod.DELETE, pathItem.getDelete(), openAPI, endpoints);
      mapOperation(path, HttpMethod.PATCH,  pathItem.getPatch(),  openAPI, endpoints);
      mapOperation(path, HttpMethod.HEAD,   pathItem.getHead(),   openAPI, endpoints);
      mapOperation(path, HttpMethod.OPTIONS,pathItem.getOptions(),openAPI, endpoints);
    }
    return endpoints;
  }

  private void mapOperation(
      String path,
      HttpMethod method,
      Operation operation,
      OpenAPI openAPI,
      List<Endpoint> out) {
    if (operation == null) {
      return;
    }

    List<Parameter> params = mapParameters(operation.getParameters());
    RequestBodySchema body = mapRequestBody(operation.getRequestBody());
    Map<Integer, ResponseSchema> responses = mapResponses(operation.getResponses());
    AuthRequirement auth = detectAuth(operation, openAPI);
    PaginationHint pagination = paginationHintDetector.detect(params);
    PayloadSizeHint payloadSize = estimatePayloadSize(operation.getRequestBody());
    SlaHint sla = extractSla(operation);
    IdFormatHint idFormat = detectIdFormat(params);
    List<String> tags = operation.getTags() != null ? List.copyOf(operation.getTags()) : List.of();

    out.add(new Endpoint(
        path, method,
        operation.getOperationId(),
        operation.getSummary(),
        params, body, responses,
        auth, pagination, payloadSize, sla, idFormat, tags));
  }

  private List<Parameter> mapParameters(
      List<io.swagger.v3.oas.models.parameters.Parameter> swaggerParams) {
    if (swaggerParams == null) {
      return List.of();
    }
    List<Parameter> result = new ArrayList<>();
    for (io.swagger.v3.oas.models.parameters.Parameter sp : swaggerParams) {
      String type = sp.getSchema() != null ? sp.getSchema().getType() : null;
      String format = sp.getSchema() != null ? sp.getSchema().getFormat() : null;
      result.add(new Parameter(
          sp.getName(),
          sp.getIn(),
          Boolean.TRUE.equals(sp.getRequired()) || "path".equals(sp.getIn()),
          type,
          format,
          sp.getDescription()));
    }
    return result;
  }

  private RequestBodySchema mapRequestBody(io.swagger.v3.oas.models.parameters.RequestBody rb) {
    if (rb == null || rb.getContent() == null) {
      return null;
    }
    Content content = rb.getContent();
    String contentType = content.keySet().stream()
        .filter(ct -> ct.contains("json"))
        .findFirst()
        .orElse(content.keySet().stream().findFirst().orElse("application/json"));
    MediaType mt = content.get(contentType);
    String schemaJson = schemaToJson(mt != null ? mt.getSchema() : null);
    return new RequestBodySchema(Boolean.TRUE.equals(rb.getRequired()), contentType, schemaJson);
  }

  private Map<Integer, ResponseSchema> mapResponses(
      io.swagger.v3.oas.models.responses.ApiResponses apiResponses) {
    if (apiResponses == null) {
      return Map.of();
    }
    Map<Integer, ResponseSchema> result = new HashMap<>();
    for (Map.Entry<String, ApiResponse> entry : apiResponses.entrySet()) {
      int code;
      try {
        code = Integer.parseInt(entry.getKey());
      } catch (NumberFormatException e) {
        code = -1; // "default"
      }
      ApiResponse resp = entry.getValue();
      String schemaJson = null;
      if (resp.getContent() != null) {
        MediaType mt = resp.getContent().values().stream().findFirst().orElse(null);
        if (mt != null) {
          schemaJson = schemaToJson(mt.getSchema());
        }
      }
      result.put(code, new ResponseSchema(code, resp.getDescription(), schemaJson));
    }
    return result;
  }

  private AuthRequirement detectAuth(Operation operation, OpenAPI openAPI) {
    List<SecurityRequirement> security = operation.getSecurity();
    // Null means "inherit from global security"; empty list means "no auth required"
    if (security == null) {
      security = openAPI.getSecurity();
    }
    if (security == null || security.isEmpty()) {
      return AuthRequirement.NONE;
    }
    for (SecurityRequirement req : security) {
      for (String schemeName : req.keySet()) {
        String lower = schemeName.toLowerCase();
        if (lower.contains("oauth") || lower.contains("bearer") || lower.contains("jwt")) {
          return AuthRequirement.BEARER_JWT;
        }
        if (lower.contains("apikey") || lower.contains("api_key") || lower.contains("x-api")) {
          return AuthRequirement.API_KEY;
        }
        if (lower.contains("basic")) {
          return AuthRequirement.BASIC;
        }
        // Peek at the scheme definition for type
        if (openAPI.getComponents() != null && openAPI.getComponents().getSecuritySchemes() != null) {
          var scheme = openAPI.getComponents().getSecuritySchemes().get(schemeName);
          if (scheme != null) {
            return switch (scheme.getType()) {
              case OAUTH2, OPENIDCONNECT -> AuthRequirement.BEARER_JWT;
              case APIKEY -> AuthRequirement.API_KEY;
              case HTTP -> "basic".equalsIgnoreCase(scheme.getScheme())
                  ? AuthRequirement.BASIC
                  : AuthRequirement.BEARER_JWT;
              default -> AuthRequirement.BEARER_JWT;
            };
          }
        }
      }
    }
    return AuthRequirement.BEARER_JWT;
  }

  private PayloadSizeHint estimatePayloadSize(io.swagger.v3.oas.models.parameters.RequestBody rb) {
    if (rb == null || rb.getContent() == null) {
      return PayloadSizeHint.unknown();
    }
    for (MediaType mt : rb.getContent().values()) {
      Schema<?> schema = mt.getSchema();
      if (schema == null) continue;
      Integer maxLength = schema.getMaxLength();
      if (maxLength != null && maxLength > 0) {
        return new PayloadSizeHint(maxLength, true);
      }
    }
    return PayloadSizeHint.unknown();
  }

  private SlaHint extractSla(Operation operation) {
    if (operation.getExtensions() == null) {
      return SlaHint.none();
    }
    Object raw = operation.getExtensions().get("x-response-time-sla");
    if (raw instanceof Number n) {
      return new SlaHint(n.longValue());
    }
    if (raw instanceof String s) {
      try {
        return new SlaHint(Long.parseLong(s));
      } catch (NumberFormatException ignored) {}
    }
    return SlaHint.none();
  }

  private IdFormatHint detectIdFormat(List<Parameter> params) {
    for (Parameter p : params) {
      String name = p.name().toLowerCase();
      if (name.equals("id") || name.endsWith("id") || name.endsWith("_id")) {
        IdFormatHint hint = idFormatDetector.detect(p.schemaFormat(), null);
        if (hint.format() != IdFormatHint.Format.UNKNOWN) {
          return hint;
        }
      }
    }
    return IdFormatHint.unknown();
  }

  @SuppressWarnings("unchecked")
  private String schemaToJson(Schema<?> schema) {
    if (schema == null) {
      return null;
    }
    try {
      return objectMapper.writeValueAsString(schema);
    } catch (Exception e) {
      log.debug("Could not serialise schema to JSON: {}", e.getMessage());
      return null;
    }
  }
}
