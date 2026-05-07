package io.github.shazaanashraff.apiforge.modules.executor;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AuthHeaderProviderTest {

  @Test
  void noneReturnsEmptyMap() {
    Map<String, String> headers = AuthHeaderProvider.headersFor(AuthRequirement.NONE, null);
    assertThat(headers).isEmpty();
  }

  @Test
  void bearerJwtAddsAuthorizationHeader() {
    Map<String, String> headers =
        AuthHeaderProvider.headersFor(AuthRequirement.BEARER_JWT, "token123");
    assertThat(headers).containsEntry("Authorization", "Bearer token123");
  }

  @Test
  void apiKeyAddsXApiKeyHeader() {
    Map<String, String> headers = AuthHeaderProvider.headersFor(AuthRequirement.API_KEY, "key123");
    assertThat(headers).containsEntry("X-Api-Key", "key123");
  }

  @Test
  void basicAddsAuthorizationHeaderWithBase64() {
    String credentials = "user:pass";
    String expected =
        "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    Map<String, String> headers = AuthHeaderProvider.headersFor(AuthRequirement.BASIC, credentials);
    assertThat(headers).containsEntry("Authorization", expected);
  }
}
