package io.github.shazaanashraff.apiforge.modules.api;

import io.github.shazaanashraff.apiforge.modules.codegenerator.CodeGenerationRequest;
import io.github.shazaanashraff.apiforge.modules.codegenerator.CodeGenerationResult;
import io.github.shazaanashraff.apiforge.modules.codegenerator.CodeGeneratorService;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/code")
public class CodeController {

  private final CodeGeneratorService codeGeneratorService;

  public CodeController(CodeGeneratorService codeGeneratorService) {
    this.codeGeneratorService = codeGeneratorService;
  }

  @PostMapping("/generate")
  public ResponseEntity<CodeGenerationResult> generate(@RequestBody CodeGenerationRequest request) {
    return ResponseEntity.ok(codeGeneratorService.generate(request));
  }

  @PostMapping("/generate/zip")
  public ResponseEntity<byte[]> generateZip(@RequestBody CodeGenerationRequest request)
      throws IOException {
    byte[] zip = codeGeneratorService.generateZip(request);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tests.zip\"")
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(zip);
  }
}
