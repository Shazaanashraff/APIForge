import type { AuthProviderProps } from 'react-oidc-context'

const keycloakBase =
  import.meta.env.VITE_KEYCLOAK_URL ?? 'http://localhost:8080'

export const oidcConfig: AuthProviderProps = {
  authority: `${keycloakBase}/realms/apiforge`,
  client_id: import.meta.env.VITE_OIDC_CLIENT_ID ?? 'apiforge-frontend',
  redirect_uri: import.meta.env.VITE_REDIRECT_URI ?? window.location.origin,
  post_logout_redirect_uri:
    import.meta.env.VITE_POST_LOGOUT_URI ?? window.location.origin,
  scope: 'openid profile email',
  automaticSilentRenew: true,
  onSigninCallback: () => {
    window.history.replaceState({}, document.title, window.location.pathname)
  },
}
