import { useEffect } from 'react'
import { useAuth } from 'react-oidc-context'
import { useAuthStore } from '@/store/authStore'

interface Props {
  children: React.ReactNode
}

export function ProtectedRoute({ children }: Props) {
  const auth = useAuth()
  const setToken = useAuthStore((s) => s.setToken)

  useEffect(() => {
    if (auth.user?.access_token) {
      setToken(auth.user.access_token, auth.user.profile.preferred_username as string)
    }
  }, [auth.user, setToken])

  if (auth.isLoading) {
    return (
      <div className="min-h-screen bg-gray-950 flex items-center justify-center">
        <span className="text-gray-400 text-sm">Loading…</span>
      </div>
    )
  }

  if (!auth.isAuthenticated) {
    auth.signinRedirect()
    return null
  }

  return <>{children}</>
}
