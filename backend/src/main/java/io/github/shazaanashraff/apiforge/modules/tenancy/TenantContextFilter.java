package io.github.shazaanashraff.apiforge.modules.tenancy;

import io.github.shazaanashraff.apiforge.modules.auth.JwtTenantExtractor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter that populates {@link TenantContextHolder} from the authenticated JWT.
 *
 * <p>Execution order: this filter is registered as a plain {@code @Component}, which Spring Boot
 * places at {@code Ordered.LOWEST_PRECEDENCE} — AFTER Spring Security's {@code FilterChainProxy}
 * (order -100). By the time this filter runs, the JWT has already been validated and the
 * {@code SecurityContextHolder} has been populated, so we can safely read from it.
 *
 * <p>The {@code finally} block ensures {@link TenantContextHolder#clear()} always runs, preventing
 * thread-local leaks when threads are reused by the servlet container.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantContextFilter extends OncePerRequestFilter {

  private final JwtTenantExtractor jwtTenantExtractor;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    try {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();

      if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Jwt jwt) {
        UUID tenantId = jwtTenantExtractor.extract(jwt);
        if (tenantId != null) {
          TenantContextHolder.set(tenantId);
          log.trace("Tenant context set: tenantId={}", tenantId);
        } else {
          log.warn("JWT present but tenantId claim is missing or invalid — path={}", request.getRequestURI());
        }
      }

      chain.doFilter(request, response);

    } finally {
      TenantContextHolder.clear();
    }
  }
}
