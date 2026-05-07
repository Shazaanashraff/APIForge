import { create } from 'zustand'
import { setAuthToken } from '@/lib/apiClient'

interface AuthState {
  token: string | null
  userName: string | null
  setToken: (token: string | null, userName?: string | null) => void
  clear: () => void
}

export const useAuthStore = create<AuthState>((set) => ({
  token: null,
  userName: null,
  setToken: (token, userName = null) => {
    setAuthToken(token)
    set({ token, userName })
  },
  clear: () => {
    setAuthToken(null)
    set({ token: null, userName: null })
  },
}))
