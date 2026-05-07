package io.github.shazaanashraff.apiforge.modules.tenancy;

import java.util.UUID;

/**
 * Thread-local store for the current request's tenant ID.
 *
 * <p>Set by {@link TenantContextFilter} at the start of each request (from the JWT {@code tenantId}
 * claim) and cleared in the finally block so the value never leaks to the next request on the same
 * thread.
 *
 * <p>Read by {@link TenantAwareQueryInterceptor} before each repository call to propagate the
 * tenant into the Postgres session variable that drives RLS.
 */
public final class TenantContextHolder {

  private static final ThreadLocal<UUID> HOLDER = new ThreadLocal<>();

  private TenantContextHolder() {}

  public static void set(UUID tenantId) {
    HOLDER.set(tenantId);
  }

  public static UUID get() {
    return HOLDER.get();
  }

  public static void clear() {
    HOLDER.remove();
  }
}
