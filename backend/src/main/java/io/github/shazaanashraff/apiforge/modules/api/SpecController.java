package io.github.shazaanashraff.apiforge.modules.api;

import io.github.shazaanashraff.apiforge.modules.schemaparser.ParsedSpec;
import io.github.shazaanashraff.apiforge.modules.schemaparser.SpecIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/specs")
public class SpecController {

  private final SpecIngestionService specIngestionService;

  public SpecController(SpecIngestionService specIngestionService) {
    this.specIngestionService = specIngestionService;
  }

  @PostMapping("/parse")
  public ResponseEntity<ParsedSpec> parseFile(@RequestParam("file") MultipartFile file) {
    return ResponseEntity.ok(specIngestionService.ingestFile(file));
  }

  @PostMapping("/introspect")
  public ResponseEntity<ParsedSpec> introspect(@RequestParam String baseUrl) {
    return ResponseEntity.ok(specIngestionService.introspect(baseUrl));
  }
}
