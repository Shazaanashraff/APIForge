package io.github.shazaanashraff.apiforge.modules.codegenerator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.shazaanashraff.apiforge.modules.testgenerator.Assertion;
import io.github.shazaanashraff.apiforge.modules.testgenerator.TestCase;
import java.util.List;
import java.util.Map;

/** Renders TestCase records as a Jest + Supertest TypeScript test file. */
class JestSupertestRenderer implements CodeGenerator {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Override
  public CodeFormat format() {
    return CodeFormat.JEST_SUPERTEST;
  }

  @Override
  public CodeGenerationResult generate(CodeGenerationRequest req) {
    return new CodeGenerationResult(
        format(),
        List.of(
            new GeneratedFile("src/" + req.className() + ".test.ts", renderTestFile(req)),
            new GeneratedFile("package.json", renderPackageJson(req.className())),
            new GeneratedFile("tsconfig.json", renderTsConfig())));
  }

  private String renderTestFile(CodeGenerationRequest req) {
    StringBuilder sb = new StringBuilder();
    sb.append("import request from 'supertest';\n\n");
    sb.append("const BASE_URL = '").append(req.baseUrl()).append("';\n\n");
    sb.append("describe('").append(req.className()).append("', () => {\n");
    int index = 0;
    for (TestCase tc : req.testCases()) {
      sb.append(renderItBlock(tc, index++));
    }
    sb.append("});\n");
    return sb.toString();
  }

  private String renderItBlock(TestCase tc, int index) {
    StringBuilder sb = new StringBuilder();
    sb.append("  it('[")
        .append(tc.category())
        .append("] ")
        .append(escapeJs(tc.description()))
        .append("', async () => {\n");
    sb.append("    const res = await request(BASE_URL)\n");
    sb.append("      .")
        .append(tc.method().name().toLowerCase())
        .append("('")
        .append(RestAssuredRenderer.resolvePath(tc))
        .append("')\n");
    if (tc.requestBody() != null) {
      sb.append("      .send(").append(bodyExpr(tc.requestBody())).append(")\n");
      sb.append("      .set('Content-Type', 'application/json')\n");
    }
    for (Map.Entry<String, String> h : tc.headers().entrySet()) {
      sb.append("      .set('")
          .append(h.getKey())
          .append("', '")
          .append(escapeJs(h.getValue()))
          .append("')\n");
    }
    sb.append("      ;\n");
    for (Assertion a : tc.expectedAssertions()) {
      sb.append(renderAssertion(a));
    }
    sb.append("  });\n\n");
    return sb.toString();
  }

  private String renderAssertion(Assertion a) {
    return switch (a.type()) {
      case STATUS_CODE -> "    expect(res.status).toBe(" + a.value() + ");\n";
      case STATUS_CODE_RANGE -> renderRange(a.value());
      case RESPONSE_TIME_MS -> "    // expected response time below " + a.value() + "ms\n";
      default -> "";
    };
  }

  private String renderRange(String range) {
    return switch (range) {
      case "2xx" ->
          "    expect(res.status).toBeGreaterThanOrEqual(200);\n"
              + "    expect(res.status).toBeLessThan(300);\n";
      case "4xx" ->
          "    expect(res.status).toBeGreaterThanOrEqual(400);\n"
              + "    expect(res.status).toBeLessThan(500);\n";
      case "5xx" ->
          "    expect(res.status).toBeGreaterThanOrEqual(500);\n"
              + "    expect(res.status).toBeLessThan(600);\n";
      default -> "    // status range: " + range + "\n";
    };
  }

  private String renderPackageJson(String className) {
    return "{\n"
        + "  \"name\": \""
        + className.toLowerCase()
        + "-tests\",\n"
        + "  \"version\": \"1.0.0\",\n"
        + "  \"description\": \"Generated API tests by APIForge\",\n"
        + "  \"scripts\": { \"test\": \"jest --runInBand\" },\n"
        + "  \"devDependencies\": {\n"
        + "    \"@types/jest\": \"^29.5.12\",\n"
        + "    \"@types/supertest\": \"^6.0.2\",\n"
        + "    \"jest\": \"^29.7.0\",\n"
        + "    \"supertest\": \"^7.0.0\",\n"
        + "    \"ts-jest\": \"^29.1.4\",\n"
        + "    \"typescript\": \"^5.4.5\"\n"
        + "  },\n"
        + "  \"jest\": { \"preset\": \"ts-jest\", \"testEnvironment\": \"node\" }\n"
        + "}\n";
  }

  private String renderTsConfig() {
    return "{\n"
        + "  \"compilerOptions\": {\n"
        + "    \"target\": \"ES2020\",\n"
        + "    \"module\": \"commonjs\",\n"
        + "    \"strict\": true,\n"
        + "    \"esModuleInterop\": true,\n"
        + "    \"skipLibCheck\": true\n"
        + "  }\n"
        + "}\n";
  }

  static String escapeJs(String s) {
    if (s == null) return "";
    return s.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "");
  }

  static String bodyExpr(Object body) {
    if (body instanceof String s) {
      if (s.isEmpty()) return "{}";
      String capped = s.length() > 512 ? s.substring(0, 512) : s;
      return "'" + escapeJs(capped) + "'";
    }
    try {
      return MAPPER.writeValueAsString(body);
    } catch (JsonProcessingException e) {
      return "{}";
    }
  }
}
