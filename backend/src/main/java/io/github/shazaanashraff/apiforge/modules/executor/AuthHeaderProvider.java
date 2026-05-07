package io.github.shazaanashraff.apiforge.modules.executor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public final class AuthHeaderProvider {

  private AuthHeaderProvider() {}

  public static Map<String, String> headersFor(AuthRequirement auth, String token) {
    return switch (auth) {
      case BEARER_JWT -> Map.of("Authorization", "Bearer " + token);
      case API_KEY -> Map.of("X-Api-Key", token);
      case BASIC -> {
        String encoded = Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
        yield Map.of("Authorization", "Basic " + encoded);
      }
      case NONE -> Map.of();
    };
  }
}
