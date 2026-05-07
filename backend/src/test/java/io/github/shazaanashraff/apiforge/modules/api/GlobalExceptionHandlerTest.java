package io.github.shazaanashraff.apiforge.modules.api;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.shazaanashraff.apiforge.modules.schemaparser.SpecParseException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void specParseExceptionReturns422() {
    ResponseEntity<ProblemDetail> response =
        handler.handleSpecParseException(new SpecParseException("bad spec", null));
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getDetail()).isEqualTo("bad spec");
    assertThat(response.getBody().getTitle()).isEqualTo("Spec Parse Error");
  }

  @Test
  void illegalArgumentExceptionReturns400() {
    ResponseEntity<ProblemDetail> response =
        handler.handleIllegalArgument(new IllegalArgumentException("invalid format"));
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getDetail()).isEqualTo("invalid format");
  }

  @Test
  void unexpectedExceptionReturns500() {
    ResponseEntity<ProblemDetail> response = handler.handleGeneral(new RuntimeException("boom"));
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getTitle()).isEqualTo("Internal Server Error");
  }
}
