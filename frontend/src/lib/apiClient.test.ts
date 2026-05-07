import { describe, it, expect, beforeEach } from 'vitest'
import { apiClient, setAuthToken } from './apiClient'

describe('apiClient', () => {
  beforeEach(() => {
    setAuthToken(null)
  })

  it('has /api as baseURL', () => {
    expect(apiClient.defaults.baseURL).toBe('/api')
  })

  it('setAuthToken sets Authorization header', () => {
    setAuthToken('my-token')
    expect(apiClient.defaults.headers.common['Authorization']).toBe('Bearer my-token')
  })

  it('setAuthToken(null) removes Authorization header', () => {
    setAuthToken('tok')
    setAuthToken(null)
    expect(apiClient.defaults.headers.common['Authorization']).toBeUndefined()
  })
})
