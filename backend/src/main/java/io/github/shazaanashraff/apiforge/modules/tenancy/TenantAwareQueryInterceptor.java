package io.github.shazaanashraff.apiforge.modules.tenancy;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * AOP aspect that propagates the current tenant into the Postgres session variable
 * {@code app.current_tenant_id} before each Spring Data repository call.
 *
 * <p>How it works:
 * <ol>
 *   <li>The pointcut fires on every public method of any {@code Repository} sub-interface.
 *   <li>If a Spring-managed transaction is already active (i.e., the repository was called from a
 *       {@code @Transactional} service method), we execute {@code SET LOCAL app.current_tenant_id}
 *       within that transaction.
 *   <li>Postgres RLS policies — which check {@code current_setting('app.current_tenant_id', TRUE)}
 *       — then automatically filter all queries to the current tenant's rows.
 * </ol>
 *
 * <p><strong>Important:</strong> {@code SET LOCAL} is a transaction-scoped command. If the
 * repository is invoked outside a transaction (e.g., directly in test code), the guard
 * {@code TransactionSynchronizationManager.isActualTransactionActive()} returns {@code false} and
 * this method returns early. Always call repositories through {@code @Transactional} service
 * methods so the RLS variable is applied correctly.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class TenantAwareQueryInterceptor {

  private final JdbcTemplate jdbcTemplate;

  @Before("execution(* org.springframework.data.repository.Repository+.*(..))")
  public void setTenantContextVariable() {
    if (!TransactionSynchronizationManager.isActualTransactionActive()) {
      // SET LOCAL has no effect outside a transaction — skip silently.
      return;
    }

    UUID tenantId = TenantContextHolder.get();
    if (tenantId == null) {
      return;
    }

    // UUID.toString() is always a safe hex-and-dash string — no injection risk.
    jdbcTemplate.execute("SET LOCAL app.current_tenant_id = '" + tenantId + "'");
    log.trace("RLS variable set: app.current_tenant_id={}", tenantId);
  }
}
