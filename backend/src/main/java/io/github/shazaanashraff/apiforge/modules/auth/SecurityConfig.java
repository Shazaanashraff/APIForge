package io.github.shazaanashraff.apiforge.modules.auth;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring Security configuration for the APIForge backend resource server.
 *
 * <p>All requests must carry a valid Bearer JWT issued by Keycloak (issuer-uri from
 * application.yml). The JWT is validated against Keycloak's JWKS endpoint automatically.
 *
 * <p>This config is NOT loaded in the "test" profile — integration tests use
 * {@code TestSecurityConfig} instead, which permits all requests so tests can call
 * the service layer without obtaining real Keycloak tokens.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("!test")
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // JWT is stateless — no session or CSRF token needed.
        .csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .authorizeHttpRequests(
            auth ->
                auth
                    // Public: health probes, OpenAPI documentation, Swagger UI.
                    .requestMatchers(
                        "/actuator/health",
                        "/actuator/info",
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**")
                    .permitAll()
                    // Actuator management endpoints (metrics, env, flyway) require ADMIN role.
                    .requestMatchers("/actuator/**")
                    .hasAuthority("ROLE_ADMIN")
                    // Everything else requires a valid JWT.
                    .anyRequest()
                    .authenticated())
        // Configure as OAuth2 Resource Server.
        // Spring Boot reads spring.security.oauth2.resourceserver.jwt.issuer-uri
        // and fetches Keycloak's JWKS automatically to validate token signatures.
        .oauth2ResourceServer(
            oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

    return http.build();
  }

  /**
   * Converts the validated JWT into a Spring Security {@code Authentication} object.
   *
   * <p>The Keycloak realm-roles protocol mapper puts role names (e.g. "ROLE_USER", "ROLE_ADMIN")
   * under the custom "roles" claim. This converter reads that claim and turns each role into a
   * {@link GrantedAuthority} so Spring Security's {@code hasAuthority()} checks work.
   */
  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(this::extractRoles);
    return converter;
  }

  private Collection<GrantedAuthority> extractRoles(Jwt jwt) {
    List<String> roles = jwt.getClaimAsStringList("roles");
    if (roles == null) {
      return List.of();
    }
    return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
  }

  /**
   * Allows the React SPA (running on Vite dev server or nginx) to call the backend.
   *
   * <p>In production, replace the allowed-origins list with your actual domain.
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(
        List.of(
            "http://localhost:5173", // Vite dev server
            "http://localhost:80" // nginx in Docker Compose
            ));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
