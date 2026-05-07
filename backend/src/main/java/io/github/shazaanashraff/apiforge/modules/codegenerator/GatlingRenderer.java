package io.github.shazaanashraff.apiforge.modules.codegenerator;

import io.github.shazaanashraff.apiforge.modules.testgenerator.Assertion;
import io.github.shazaanashraff.apiforge.modules.testgenerator.TestCase;
import java.util.List;
import java.util.Map;

/** Renders TestCase records as a Gatling 3.x Java DSL simulation. */
class GatlingRenderer implements CodeGenerator {

  @Override
  public CodeFormat format() {
    return CodeFormat.GATLING;
  }

  @Override
  public CodeGenerationResult generate(CodeGenerationRequest req) {
    return new CodeGenerationResult(
        format(),
        List.of(
            new GeneratedFile(
                "src/test/java/io/apiforge/generated/" + req.className() + "Simulation.java",
                renderSimulation(req)),
            new GeneratedFile("pom.xml", renderPom(req.className()))));
  }

  private String renderSimulation(CodeGenerationRequest req) {
    StringBuilder sb = new StringBuilder();
    sb.append("package io.apiforge.generated;\n\n");
    sb.append("import io.gatling.javaapi.core.*;\n");
    sb.append("import io.gatling.javaapi.http.*;\n");
    sb.append("import static io.gatling.javaapi.core.CoreDsl.*;\n");
    sb.append("import static io.gatling.javaapi.http.HttpDsl.*;\n\n");
    sb.append("public class ")
        .append(req.className())
        .append("Simulation extends Simulation {\n\n");
    sb.append("  HttpProtocolBuilder httpProtocol =\n");
    sb.append("      http.baseUrl(\"")
        .append(req.baseUrl())
        .append("\").acceptHeader(\"application/json\");\n\n");
    sb.append("  ScenarioBuilder scn =\n");
    sb.append("      scenario(\"").append(req.className()).append("\")\n");
    for (TestCase tc : req.testCases()) {
      sb.append("          .exec(\n");
      sb.append("              http(\"[")
          .append(tc.category())
          .append("] ")
          .append(RestAssuredRenderer.escapeJava(tc.description()))
          .append("\")\n");
      sb.append("                  .")
          .append(tc.method().name().toLowerCase())
          .append("(\"")
          .append(RestAssuredRenderer.resolvePath(tc))
          .append("\")\n");
      if (tc.requestBody() != null) {
        sb.append("                  .body(StringBody(")
            .append(RestAssuredRenderer.bodyExpr(tc.requestBody()))
            .append(")).asJson()\n");
      }
      for (Map.Entry<String, String> h : tc.headers().entrySet()) {
        sb.append("                  .header(\"")
            .append(h.getKey())
            .append("\", \"")
            .append(RestAssuredRenderer.escapeJava(h.getValue()))
            .append("\")\n");
      }
      sb.append("                  .check(status().in(")
          .append(renderStatusList(tc))
          .append(")))\n");
    }
    sb.append("      ;\n\n");
    sb.append("  {\n");
    sb.append("    setUp(scn.injectOpen(atOnceUsers(1))).protocols(httpProtocol);\n");
    sb.append("  }\n");
    sb.append("}\n");
    return sb.toString();
  }

  private String renderStatusList(TestCase tc) {
    for (Assertion a : tc.expectedAssertions()) {
      if (a.type() == Assertion.AssertionType.STATUS_CODE) {
        return a.value();
      }
      if (a.type() == Assertion.AssertionType.STATUS_CODE_RANGE) {
        return switch (a.value()) {
          case "2xx" -> "200, 201, 202, 204";
          case "4xx" -> "400, 401, 403, 404, 422, 429";
          case "5xx" -> "500, 502, 503, 504";
          default -> "200";
        };
      }
    }
    return "200";
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
        + "-gatling</artifactId>\n"
        + "  <version>1.0.0</version>\n"
        + "  <dependencies>\n"
        + "    <dependency>\n"
        + "      <groupId>io.gatling.highcharts</groupId>\n"
        + "      <artifactId>gatling-charts-highcharts</artifactId>\n"
        + "      <version>3.10.3</version>\n"
        + "      <scope>test</scope>\n"
        + "    </dependency>\n"
        + "  </dependencies>\n"
        + "  <build>\n"
        + "    <testSourceDirectory>src/test/java</testSourceDirectory>\n"
        + "    <plugins>\n"
        + "      <plugin>\n"
        + "        <groupId>io.gatling</groupId>\n"
        + "        <artifactId>gatling-maven-plugin</artifactId>\n"
        + "        <version>4.9.2</version>\n"
        + "        <configuration>\n"
        + "          <simulationClass>io.apiforge.generated."
        + className
        + "Simulation</simulationClass>\n"
        + "        </configuration>\n"
        + "        <executions>\n"
        + "          <execution>\n"
        + "            <goals><goal>test</goal></goals>\n"
        + "          </execution>\n"
        + "        </executions>\n"
        + "      </plugin>\n"
        + "    </plugins>\n"
        + "  </build>\n"
        + "</project>\n";
  }
}
