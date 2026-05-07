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

class PostmanParserTest {

  private PostmanParser parser;

  @BeforeEach
  void setUp() {
    ObjectMapper objectMapper = new ObjectMapper();
    PaginationHintDetector paginationDetector = new PaginationHintDetector();
    IdFormatDetector idFormatDetector = new IdFormatDetector();
    parser = new PostmanParser(objectMapper, paginationDetector, idFormatDetector);
  }

  @Test
  void parsesAllItemsFromSampleCollection() throws IOException {
    String json = Files.readString(Path.of("../examples/sample-postman-collection.json"));

    List<Endpoint> endpoints = parser.parse(json);

    // Auth: 1 item (Login), Products: 3 items (List, Create, GetById) = 4 total
    assertThat(endpoints).hasSize(4);
  }

  @Test
  void loginRequestIsMappedCorrectly() throws IOException {
    String json = Files.readString(Path.of("../examples/sample-postman-collection.json"));

    List<Endpoint> endpoints = parser.parse(json);

    Endpoint login =
        endpoints.stream().filter(e -> "Login".equals(e.summary())).findFirst().orElseThrow();

    assertThat(login.method()).isEqualTo(HttpMethod.POST);
    assertThat(login.path()).contains("auth");
    assertThat(login.requestBody()).isNotNull();
    assertThat(login.authRequirement()).isEqualTo(AuthRequirement.NONE);
  }

  @Test
  void listProductsHasPaginationHint() throws IOException {
    String json = Files.readString(Path.of("../examples/sample-postman-collection.json"));

    List<Endpoint> endpoints = parser.parse(json);

    Endpoint listProducts =
        endpoints.stream()
            .filter(e -> "List Products".equals(e.summary()))
            .findFirst()
            .orElseThrow();

    assertThat(listProducts.method()).isEqualTo(HttpMethod.GET);
    assertThat(listProducts.paginationHint().style()).isEqualTo(PaginationHint.Style.PAGE_SIZE);
    // The 'Authorization: Bearer {{token}}' header should be detected
    assertThat(listProducts.authRequirement()).isEqualTo(AuthRequirement.BEARER_JWT);
  }

  @Test
  void createProductIsMappedCorrectly() throws IOException {
    String json = Files.readString(Path.of("../examples/sample-postman-collection.json"));

    List<Endpoint> endpoints = parser.parse(json);

    Endpoint createProduct =
        endpoints.stream()
            .filter(e -> "Create Product".equals(e.summary()))
            .findFirst()
            .orElseThrow();

    assertThat(createProduct.method()).isEqualTo(HttpMethod.POST);
    assertThat(createProduct.requestBody()).isNotNull();
    assertThat(createProduct.authRequirement()).isEqualTo(AuthRequirement.BEARER_JWT);
  }

  @Test
  void throwsOnInvalidJson() {
    assertThatThrownBy(() -> parser.parse("not json")).isInstanceOf(SpecParseException.class);
  }
}
