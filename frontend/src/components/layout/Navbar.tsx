import { useAuth } from 'react-oidc-context'

export function Navbar() {
  const auth = useAuth()
  const name =
    (auth.user?.profile.preferred_username as string | undefined) ??
    auth.user?.profile.email ??
    'User'

  return (
    <header className="h-12 shrink-0 flex items-center justify-end px-6 bg-gray-900 border-b border-gray-800">
      <div className="flex items-center gap-4">
        <span className="text-sm text-gray-400">{name}</span>
        <button
          onClick={() => auth.signoutRedirect()}
          className="text-xs text-gray-500 hover:text-white transition-colors"
        >
          Sign out
        </button>
      </div>
    </header>
  )
}
