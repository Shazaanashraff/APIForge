package io.github.shazaanashraff.apiforge.modules.executor;

import io.github.shazaanashraff.apiforge.modules.testgenerator.TestCase;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

class HttpRequestBuilder {

  private HttpRequestBuilder() {}

  static WebClient.RequestHeadersSpec<?> build(
      WebClient client,
      TestCase tc,
      String baseUrl,
      Map<String, String> authHeaders,
      VariableStore vars) {
    String uri = buildUri(tc, baseUrl, vars);
    Map<String, String> headers = buildHeaders(tc, authHeaders);

    WebClient.RequestBodySpec spec = client.method(tc.method()).uri(uri);
    for (Map.Entry<String, String> e : headers.entrySet()) {
      spec = spec.header(e.getKey(), e.getValue());
    }

    if (tc.requestBody() != null) {
      return spec.bodyValue(tc.requestBody());
    }
    return spec;
  }

  static String buildUri(TestCase tc, String baseUrl, VariableStore vars) {
    String path = tc.endpointPath();
    if (tc.pathParams() != null) {
      for (Map.Entry<String, String> e : tc.pathParams().entrySet()) {
        path = path.replace("{" + e.getKey() + "}", vars.interpolate(e.getValue()));
      }
    }
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl + path);
    if (tc.queryParams() != null) {
      for (Map.Entry<String, String> e : tc.queryParams().entrySet()) {
        builder.queryParam(e.getKey(), vars.interpolate(e.getValue()));
      }
    }
    return builder.build().toUriString();
  }

  static Map<String, String> buildHeaders(TestCase tc, Map<String, String> authHeaders) {
    Map<String, String> headers = new HashMap<>();
    if (authHeaders != null) headers.putAll(authHeaders);
    if (tc.headers() != null) headers.putAll(tc.headers());
    if (tc.requestBody() != null && !headers.containsKey("Content-Type")) {
      headers.put("Content-Type", "application/json");
    }
    return headers;
  }
}
