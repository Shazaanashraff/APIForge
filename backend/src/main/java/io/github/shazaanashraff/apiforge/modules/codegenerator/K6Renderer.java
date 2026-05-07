package io.github.shazaanashraff.apiforge.modules.codegenerator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.shazaanashraff.apiforge.modules.testgenerator.Assertion;
import io.github.shazaanashraff.apiforge.modules.testgenerator.TestCase;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpMethod;

/** Renders TestCase records as a k6 JavaScript load-test script. */
class K6Renderer implements CodeGenerator {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Override
  public CodeFormat format() {
    return CodeFormat.K6;
  }

  @Override
  public CodeGenerationResult generate(CodeGenerationRequest req) {
    return new CodeGenerationResult(
        format(), List.of(new GeneratedFile(req.className() + "-k6.js", renderScript(req))));
  }

  private String renderScript(CodeGenerationRequest req) {
    StringBuilder sb = new StringBuilder();
    sb.append("import http from 'k6/http';\n");
    sb.append("import { check, sleep } from 'k6';\n\n");
    sb.append("export const options = {\n");
    sb.append("  vus: 10,\n");
    sb.append("  duration: '30s',\n");
    sb.append("};\n\n");
    sb.append("const BASE_URL = '").append(req.baseUrl()).append("';\n\n");
    sb.append("export default function () {\n");
    int index = 0;
    for (TestCase tc : req.testCases()) {
      sb.append(renderTestCase(tc, index++));
    }
    sb.append("}\n");
    return sb.toString();
  }

  private String renderTestCase(TestCase tc, int index) {
    StringBuilder sb = new StringBuilder();
    sb.append("  // [").append(tc.category()).append("] ").append(tc.description()).append("\n");

    String method = tc.method().name().toLowerCase();
    String path = RestAssuredRenderer.resolvePath(tc);
    String qs = buildQueryString(tc.queryParams());
    String url = "${BASE_URL}" + path + (qs.isEmpty() ? "" : "?" + qs);

    boolean methodHasBody =
        tc.method() == HttpMethod.POST
            || tc.method() == HttpMethod.PUT
            || tc.method() == HttpMethod.PATCH;
    boolean hasHeaders = !tc.headers().isEmpty();

    sb.append("  const res")
        .append(index)
        .append(" = http.")
        .append(method)
        .append("(`")
        .append(url)
        .append("`");

    if (methodHasBody) {
      if (tc.requestBody() != null) {
        sb.append(", JSON.stringify(").append(bodyExpr(tc.requestBody())).append(")");
      } else {
        sb.append(", null");
      }
    }

    if (hasHeaders || (methodHasBody && tc.requestBody() != null)) {
      sb.append(", { headers: {");
      if (methodHasBody && tc.requestBody() != null) {
        sb.append(" 'Content-Type': 'application/json'");
        if (hasHeaders) sb.append(",");
      }
      boolean first = !methodHasBody || tc.requestBody() == null;
      for (Map.Entry<String, String> h : tc.headers().entrySet()) {
        if (!first) sb.append(",");
        sb.append(" '")
            .append(h.getKey())
            .append("': '")
            .append(JestSupertestRenderer.escapeJs(h.getValue()))
            .append("'");
        first = false;
      }
      sb.append(" } }");
    }

    sb.append(");\n");
    sb.append("  check(res").append(index).append(", {\n");
    for (Assertion a : tc.expectedAssertions()) {
      sb.append(renderCheck(a));
    }
    sb.append("  });\n");
    sb.append("  sleep(0.1);\n\n");
    return sb.toString();
  }

  private String renderCheck(Assertion a) {
    return switch (a.type()) {
      case STATUS_CODE ->
          "    'status is " + a.value() + "': (r) => r.status === " + a.value() + ",\n";
      case STATUS_CODE_RANGE -> renderRangeCheck(a.value());
      case RESPONSE_TIME_MS ->
          "    'response time < "
              + a.value()
              + "ms': (r) => r.timings.duration < "
              + a.value()
              + ",\n";
      default -> "";
    };
  }

  private String renderRangeCheck(String range) {
    return switch (range) {
      case "2xx" -> "    'status is 2xx': (r) => r.status >= 200 && r.status < 300,\n";
      case "4xx" -> "    'status is 4xx': (r) => r.status >= 400 && r.status < 500,\n";
      case "5xx" -> "    'status is 5xx': (r) => r.status >= 500 && r.status < 600,\n";
      default -> "    'status in range': (r) => r.status >= 100,\n";
    };
  }

  private static String buildQueryString(Map<String, String> params) {
    if (params.isEmpty()) return "";
    StringBuilder qs = new StringBuilder();
    for (Map.Entry<String, String> e : params.entrySet()) {
      if (!qs.isEmpty()) qs.append("&");
      qs.append(e.getKey()).append("=").append(e.getValue());
    }
    return qs.toString();
  }

  static String bodyExpr(Object body) {
    if (body instanceof String s) {
      if (s.isEmpty()) return "{}";
      if (s.chars().distinct().count() == 1) {
        return "'" + s.charAt(0) + "'.repeat(" + s.length() + ")";
      }
      String capped = s.length() > 512 ? s.substring(0, 512) : s;
      return "'" + JestSupertestRenderer.escapeJs(capped) + "'";
    }
    try {
      return MAPPER.writeValueAsString(body);
    } catch (JsonProcessingException e) {
      return "{}";
    }
  }
}
