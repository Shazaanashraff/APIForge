package io.github.shazaanashraff.apiforge.modules.codegenerator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.shazaanashraff.apiforge.modules.testgenerator.Assertion;
import io.github.shazaanashraff.apiforge.modules.testgenerator.TestCase;
import java.util.List;
import java.util.Map;

/** Renders TestCase records as a JUnit 5 + RestAssured Java test class. */
class RestAssuredRenderer implements CodeGenerator {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Override
  public CodeFormat format() {
    return CodeFormat.JUNIT5_REST_ASSURED;
  }

  @Override
  public CodeGenerationResult generate(CodeGenerationRequest req) {
    return new CodeGenerationResult(
        format(),
        List.of(
            new GeneratedFile(
                "src/test/java/io/apiforge/generated/" + req.className() + "Test.java",
                renderTestClass(req)),
            new GeneratedFile("pom.xml", renderPom(req.className()))));
  }

  private String renderTestClass(CodeGenerationRequest req) {
    StringBuilder sb = new StringBuilder();
    sb.append("package io.apiforge.generated;\n\n");
    sb.append("import io.restassured.RestAssured;\n");
    sb.append("import io.restassured.http.ContentType;\n");
    sb.append("import org.junit.jupiter.api.BeforeAll;\n");
    sb.append("import org.junit.jupiter.api.DisplayName;\n");
    sb.append("import org.junit.jupiter.api.Test;\n");
    sb.append("import static io.restassured.RestAssured.given;\n");
    sb.append("import static org.hamcrest.Matchers.*;\n\n");
    sb.append("public class ").append(req.className()).append("Test {\n\n");
    sb.append("    @BeforeAll\n");
    sb.append("    static void setup() {\n");
    sb.append("        RestAssured.baseURI = \"").append(req.baseUrl()).append("\";\n");
    sb.append("    }\n\n");
    int index = 1;
    for (TestCase tc : req.testCases()) {
      sb.append(renderTestMethod(tc, index++));
    }
    sb.append("}\n");
    return sb.toString();
  }

  private String renderTestMethod(TestCase tc, int index) {
    StringBuilder sb = new StringBuilder();
    sb.append("    @Test\n");
    sb.append("    @DisplayName(\"[")
        .append(tc.category())
        .append("] ")
        .append(escapeJava(tc.description()))
        .append("\")\n");
    sb.append("    void test").append(index).append("_").append(sanitize(tc.id())).append("() {\n");
    sb.append("        given()\n");
    for (Map.Entry<String, String> h : tc.headers().entrySet()) {
      sb.append("            .header(\"")
          .append(h.getKey())
          .append("\", \"")
          .append(escapeJava(h.getValue()))
          .append("\")\n");
    }
    for (Map.Entry<String, String> q : tc.queryParams().entrySet()) {
      sb.append("            .queryParam(\"")
          .append(q.getKey())
          .append("\", \"")
          .append(escapeJava(q.getValue()))
          .append("\")\n");
    }
    if (tc.requestBody() != null) {
      sb.append("            .contentType(ContentType.JSON)\n");
      sb.append("            .body(").append(bodyExpr(tc.requestBody())).append(")\n");
    }
    sb.append("        .when()\n");
    sb.append("            .")
        .append(tc.method().name().toLowerCase())
        .append("(\"")
        .append(resolvePath(tc))
        .append("\")\n");
    sb.append("        .then()\n");
    for (Assertion a : tc.expectedAssertions()) {
      sb.append(renderAssertion(a));
    }
    sb.append("            ;\n");
    sb.append("    }\n\n");
    return sb.toString();
  }

  private String renderAssertion(Assertion a) {
    return switch (a.type()) {
      case STATUS_CODE -> "            .statusCode(" + a.value() + ")\n";
      case STATUS_CODE_RANGE -> renderRange(a.value());
      case RESPONSE_TIME_MS -> "            // expected response time below " + a.value() + "ms\n";
      default -> "";
    };
  }

  private String renderRange(String range) {
    return switch (range) {
      case "2xx" -> "            .statusCode(allOf(greaterThanOrEqualTo(200), lessThan(300)))\n";
      case "3xx" -> "            .statusCode(allOf(greaterThanOrEqualTo(300), lessThan(400)))\n";
      case "4xx" -> "            .statusCode(allOf(greaterThanOrEqualTo(400), lessThan(500)))\n";
      case "5xx" -> "            .statusCode(allOf(greaterThanOrEqualTo(500), lessThan(600)))\n";
      default -> "            // status range: " + range + "\n";
    };
  }

  private String renderPom(String className) {
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
        + "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        + "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0"
        + " https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
        + "  <modelVersion>4.0.0</modelVersion>\n"
        + "  <groupId>io.apiforge.generated</groupId>\n"
        + "  <artifactId>"
        + className
        + "-tests</artifactId>\n"
        + "  <version>1.0.0</version>\n"
        + "  <dependencies>\n"
        + "    <dependency>\n"
        + "      <groupId>io.rest-assured</groupId>\n"
        + "      <artifactId>rest-assured</artifactId>\n"
        + "      <version>5.4.0</version>\n"
        + "      <scope>test</scope>\n"
        + "    </dependency>\n"
        + "    <dependency>\n"
        + "      <groupId>org.junit.jupiter</groupId>\n"
        + "      <artifactId>junit-jupiter</artifactId>\n"
        + "      <version>5.10.2</version>\n"
        + "      <scope>test</scope>\n"
        + "    </dependency>\n"
        + "  </dependencies>\n"
        + "  <build>\n"
        + "    <plugins>\n"
        + "      <plugin>\n"
        + "        <groupId>org.apache.maven.plugins</groupId>\n"
        + "        <artifactId>maven-surefire-plugin</artifactId>\n"
        + "        <version>3.2.5</version>\n"
        + "      </plugin>\n"
        + "      <plugin>\n"
        + "        <groupId>org.apache.maven.plugins</groupId>\n"
        + "        <artifactId>maven-compiler-plugin</artifactId>\n"
        + "        <version>3.12.1</version>\n"
        + "        <configuration>\n"
        + "          <source>17</source>\n"
        + "          <target>17</target>\n"
        + "        </configuration>\n"
        + "      </plugin>\n"
        + "    </plugins>\n"
        + "  </build>\n"
        + "</project>\n";
  }

  // ── Package-private helpers reused by other renderers ─────────────────────

  static String resolvePath(TestCase tc) {
    String path = tc.endpointPath();
    for (Map.Entry<String, String> e : tc.pathParams().entrySet()) {
      path = path.replace("{" + e.getKey() + "}", e.getValue());
    }
    return path;
  }

  static String sanitize(String id) {
    return id == null ? "x" : id.replaceAll("[^a-zA-Z0-9]", "_");
  }

  static String escapeJava(String s) {
    if (s == null) return "";
    return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
  }

  static String bodyExpr(Object body) {
    if (body instanceof String s) {
      if (s.isEmpty()) return "\"\"";
      if (s.chars().distinct().count() == 1) {
        return "\"" + s.charAt(0) + "\".repeat(" + s.length() + ")";
      }
      String capped = s.length() > 512 ? s.substring(0, 512) : s;
      return "\"" + escapeJava(capped) + "\"";
    }
    try {
      return "\"" + escapeJava(MAPPER.writeValueAsString(body)) + "\"";
    } catch (JsonProcessingException e) {
      return "\"{}\"";
    }
  }
}
