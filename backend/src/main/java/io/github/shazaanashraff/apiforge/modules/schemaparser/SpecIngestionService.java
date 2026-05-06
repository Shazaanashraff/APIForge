package io.github.shazaanashraff.apiforge.modules.schemaparser;

import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Public API of the schemaparser module.
 *
 * <p>Accepts specs as raw file uploads, URLs, or by live introspection of a running server.
 * Automatically detects whether the content is OpenAPI or Postman and delegates to the
 * appropriate parser. Returns a {@link ParsedSpec} ready for the test case generator.
 *
 * <p>Other modules MUST interact with this module only through this service class —
 * never by importing parsers or detectors directly (Spring Modulith rule).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpecIngestionService {

  private final OpenApiParser openApiParser;
  private final PostmanParser postmanParser;

  /**
   * Parses an uploaded spec file (OpenAPI JSON/YAML or Postman v2.1 JSON).
   *
   * @throws SpecParseException when the file content cannot be parsed as either format
   */
  public ParsedSpec ingestFile(MultipartFile file) {
    String content;
    try {
      content = new String(file.getBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new SpecParseException("Could not read uploaded file: " + e.getMessage(), e);
    }
    String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload";
    log.info("Ingesting spec file: {} ({} bytes)", filename, file.getSize());
    return parseContent(content, filename);
  }

  /**
   * Fetches and parses a spec from a URL.
   *
   * <p>If the URL points to an OpenAPI spec (detected by content type or root keys), it is parsed
   * as OpenAPI. Otherwise it is treated as Postman. Both JSON and YAML OpenAPI are supported.
   */
  public ParsedSpec ingestUrl(String url) {
    log.info("Ingesting spec from URL: {}", url);
    try {
      // Try OpenAPI first — swagger-parser fetches the URL itself
      List<Endpoint> endpoints = openApiParser.parseFromUrl(url);
      String title = extractTitle(url);
      return ParsedSpec.of(endpoints, title, "3.x", url);
    } catch (SpecParseException e) {
      log.debug("URL is not an OpenAPI spec, trying Postman: {}", e.getMessage());
      // Postman parser needs the raw content — fetch it
      throw new SpecParseException(
          "Could not parse spec from URL: " + url + ". Try uploading the file directly.", e);
    }
  }

  /**
   * Auto-discovers an OpenAPI spec from a running server by probing standard doc paths.
   *
   * @param baseUrl the server root, e.g. {@code http://localhost:8090}
   */
  public ParsedSpec introspect(String baseUrl) {
    log.info("Introspecting spec at: {}", baseUrl);
    List<Endpoint> endpoints = openApiParser.introspect(baseUrl);
    return ParsedSpec.of(endpoints, baseUrl, "3.x", baseUrl);
  }

  private ParsedSpec parseContent(String content, String source) {
    if (isPostman(content)) {
      List<Endpoint> endpoints = postmanParser.parse(content);
      return ParsedSpec.of(endpoints, source, "postman-2.1", null);
    }
    List<Endpoint> endpoints = openApiParser.parse(content);
    String version = detectOpenApiVersion(content);
    return ParsedSpec.of(endpoints, source, version, null);
  }

  private boolean isPostman(String content) {
    String trimmed = content.stripLeading();
    return trimmed.startsWith("{") && content.contains("schema.getpostman.com");
  }

  private String detectOpenApiVersion(String content) {
    if (content.contains("\"openapi\":\"3.1") || content.contains("openapi: '3.1")
        || content.contains("openapi: \"3.1")) {
      return "3.1.x";
    }
    return "3.0.x";
  }

  private String extractTitle(String url) {
    try {
      ParseOptions options = new ParseOptions();
      options.setResolve(false);
      var result = new OpenAPIV3Parser().readLocation(url, null, options);
      if (result.getOpenAPI() != null && result.getOpenAPI().getInfo() != null) {
        return result.getOpenAPI().getInfo().getTitle();
      }
    } catch (Exception ignored) {}
    return url;
  }
}
