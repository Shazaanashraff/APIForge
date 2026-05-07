package io.github.shazaanashraff.apiforge.modules.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.shazaanashraff.apiforge.modules.schemaparser.ParsedSpec;
import io.github.shazaanashraff.apiforge.modules.schemaparser.SpecIngestionService;
import io.github.shazaanashraff.apiforge.modules.schemaparser.SpecParseException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class SpecControllerTest {

  @Mock private SpecIngestionService specIngestionService;
  @InjectMocks private SpecController controller;

  private MockMvc mvc;

  @BeforeEach
  void setup() {
    mvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  void parseFileReturns200WithParsedSpec() throws Exception {
    ParsedSpec spec = ParsedSpec.of(List.of(), "test-spec", "3.0.x", null);
    when(specIngestionService.ingestFile(any())).thenReturn(spec);

    MockMultipartFile file =
        new MockMultipartFile("file", "openapi.json", "application/json", "{}".getBytes());
    mvc.perform(multipart("/api/specs/parse").file(file))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("test-spec"));
  }

  @Test
  void parseFileReturns422OnSpecParseException() throws Exception {
    when(specIngestionService.ingestFile(any()))
        .thenThrow(new SpecParseException("invalid spec", null));

    MockMultipartFile file =
        new MockMultipartFile("file", "bad.json", "application/json", "bad".getBytes());
    mvc.perform(multipart("/api/specs/parse").file(file))
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  void parseFileReturnsEndpointCount() throws Exception {
    ParsedSpec spec = ParsedSpec.of(List.of(), "petstore", "3.0.x", null);
    when(specIngestionService.ingestFile(any())).thenReturn(spec);

    MockMultipartFile file =
        new MockMultipartFile(
            "file", "petstore.yaml", "application/yaml", "openapi: 3.0.0".getBytes());
    mvc.perform(multipart("/api/specs/parse").file(file))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.endpoints").isArray());
  }
}
