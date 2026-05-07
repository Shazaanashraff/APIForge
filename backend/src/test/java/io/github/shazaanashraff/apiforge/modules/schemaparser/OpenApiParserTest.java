package io.github.shazaanashraff.apiforge.modules.schemaparser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

class OpenApiParserTest {

  private OpenApiParser parser;

  @BeforeEach
  void setUp() {
    PaginationHintDetector paginationDetector = new PaginationHintDetector();
    IdFormatDetector idFormatDetector = new IdFormatDetector();
    ObjectMapper objectMapper = new ObjectMapper();
    EndpointMapper mapper = new EndpointMapper(paginationDetector, idFormatDetector, objectMapper);
    parser = new OpenApiParser(mapper);
  }

  @Test
  void parsesAllPetstoreEndpoints() throws IOException {
    String spec = Files.readString(Path.of("../examples/petstore-openapi.yaml"));

    List<Endpoint> endpoints = parser.parse(spec);

    // Petstore 3.0 has 19 operations across 13 paths
    assertThat(endpoints).hasSize(19);
  }

  @Test
  void petstoreEndpointsHaveExpectedMethods() throws IOException {
    String spec = Files.readString(Path.of("../examples/petstore-openapi.yaml"));

    List<Endpoint> endpoints = parser.parse(spec);

    assertThat(endpoints).anyMatch(e -> e.method() == HttpMethod.GET && e.path().contains("/pet/"));
    assertThat(endpoints).anyMatch(e -> e.method() == HttpMethod.POST && "/pet".equals(e.path()));
    assertThat(endpoints).anyMatch(e -> e.method() == HttpMethod.PUT && "/pet".equals(e.path()));
    assertThat(endpoints).anyMatch(e -> e.method() == HttpMethod.DELETE);
  }

  @Test
  void petstoreEndpointsHaveTags() throws IOException {
    String spec = Files.readString(Path.of("../examples/petstore-openapi.yaml"));

    List<Endpoint> endpoints = parser.parse(spec);

    assertThat(endpoints).allMatch(e -> e.tags() != null && !e.tags().isEmpty());
    assertThat(endpoints).anyMatch(e -> e.tags().contains("pet"));
    assertThat(endpoints).anyMatch(e -> e.tags().contains("store"));
    assertThat(endpoints).anyMatch(e -> e.tags().contains("user"));
  }

  @Test
  void petstoreAuthRequirementsDetected() throws IOException {
    String spec = Files.readString(Path.of("../examples/petstore-openapi.yaml"));

    List<Endpoint> endpoints = parser.parse(spec);

    // petstore_auth is OAuth2 → BEARER_JWT; api_key → API_KEY; /store/inventory uses api_key
    assertThat(endpoints).anyMatch(e -> e.authRequirement() == AuthRequirement.BEARER_JWT);
    assertThat(endpoints).anyMatch(e -> e.authRequirement() == AuthRequirement.API_KEY);
  }

  @Test
  void petstorePetByIdHasPathParameter() throws IOException {
    String spec = Files.readString(Path.of("../examples/petstore-openapi.yaml"));

    List<Endpoint> endpoints = parser.parse(spec);

    Endpoint getPetById =
        endpoints.stream()
            .filter(e -> "/pet/{petId}".equals(e.path()) && e.method() == HttpMethod.GET)
            .findFirst()
            .orElseThrow(() -> new AssertionError("GET /pet/{petId} not found"));

    assertThat(getPetById.parameters())
        .anyMatch(p -> "petId".equals(p.name()) && "path".equals(p.in()) && p.required());
  }

  @Test
  void throwsSpecParseExceptionForGarbage() {
    assertThatThrownBy(() -> parser.parse("this is not a spec"))
        .isInstanceOf(SpecParseException.class);
  }
}
