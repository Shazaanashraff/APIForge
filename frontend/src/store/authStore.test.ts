import { describe, it, expect, beforeEach } from 'vitest'
import { useAuthStore } from './authStore'
import { apiClient } from '@/lib/apiClient'

describe('authStore', () => {
  beforeEach(() => {
    useAuthStore.getState().clear()
  })

  it('setToken stores token and userName', () => {
    useAuthStore.getState().setToken('abc', 'alice')
    const { token, userName } = useAuthStore.getState()
    expect(token).toBe('abc')
    expect(userName).toBe('alice')
  })

  it('setToken propagates to apiClient header', () => {
    useAuthStore.getState().setToken('xyz')
    expect(apiClient.defaults.headers.common['Authorization']).toBe('Bearer xyz')
  })

  it('clear resets state and removes header', () => {
    useAuthStore.getState().setToken('tok', 'bob')
    useAuthStore.getState().clear()
    expect(useAuthStore.getState().token).toBeNull()
    expect(apiClient.defaults.headers.common['Authorization']).toBeUndefined()
  })
})
