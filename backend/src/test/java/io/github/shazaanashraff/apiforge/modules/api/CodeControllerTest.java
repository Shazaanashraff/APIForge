package io.github.shazaanashraff.apiforge.modules.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.shazaanashraff.apiforge.modules.codegenerator.CodeFormat;
import io.github.shazaanashraff.apiforge.modules.codegenerator.CodeGenerationResult;
import io.github.shazaanashraff.apiforge.modules.codegenerator.CodeGeneratorService;
import io.github.shazaanashraff.apiforge.modules.codegenerator.GeneratedFile;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class CodeControllerTest {

  @Mock private CodeGeneratorService codeGeneratorService;
  @InjectMocks private CodeController controller;

  private MockMvc mvc;

  @BeforeEach
  void setup() {
    mvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  void generateReturns200WithResult() throws Exception {
    CodeGenerationResult result =
        new CodeGenerationResult(
            CodeFormat.JUNIT5_REST_ASSURED,
            List.of(new GeneratedFile("TestSuite.java", "// code")));
    when(codeGeneratorService.generate(any())).thenReturn(result);

    mvc.perform(
            post("/api/code/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"testCases\":[],\"baseUrl\":\"http://localhost\",\"className\":\"Tests\",\"format\":\"JUNIT5_REST_ASSURED\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.format").value("JUNIT5_REST_ASSURED"));
  }

  @Test
  void generateZipReturnsOctetStream() throws Exception {
    when(codeGeneratorService.generateZip(any())).thenReturn(new byte[] {1, 2, 3});

    mvc.perform(
            post("/api/code/generate/zip")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"testCases\":[],\"baseUrl\":\"http://localhost\",\"className\":\"Tests\",\"format\":\"K6\"}"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
  }

  @Test
  void generateZipHasContentDispositionHeader() throws Exception {
    when(codeGeneratorService.generateZip(any())).thenReturn(new byte[0]);

    mvc.perform(
            post("/api/code/generate/zip")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"testCases\":[],\"baseUrl\":\"http://localhost\",\"className\":\"Tests\",\"format\":\"GATLING\"}"))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Disposition", "attachment; filename=\"tests.zip\""));
  }
}
