package io.github.shazaanashraff.apiforge.modules.codegenerator;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Public module API for the code generator.
 *
 * <p>Given a {@link CodeGenerationRequest} (test cases + target URL + format), produces a {@link
 * CodeGenerationResult} containing ready-to-run test files, or a ZIP archive of those files.
 */
@Service
public class CodeGeneratorService {

  private final Map<CodeFormat, CodeGenerator> renderers = new EnumMap<>(CodeFormat.class);

  public CodeGeneratorService() {
    renderers.put(CodeFormat.JUNIT5_REST_ASSURED, new RestAssuredRenderer());
    renderers.put(CodeFormat.JEST_SUPERTEST, new JestSupertestRenderer());
    renderers.put(CodeFormat.K6, new K6Renderer());
    renderers.put(CodeFormat.GATLING, new GatlingRenderer());
  }

  public CodeGenerationResult generate(CodeGenerationRequest request) {
    CodeGenerator renderer = renderers.get(request.format());
    if (renderer == null) {
      throw new IllegalArgumentException("Unsupported code format: " + request.format());
    }
    return renderer.generate(request);
  }

  public byte[] generateZip(CodeGenerationRequest request) throws IOException {
    return TestFileWriter.toZip(generate(request));
  }
}
