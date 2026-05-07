import { describe, it, expect, vi, beforeEach } from 'vitest'
import { apiClient } from '@/lib/apiClient'
import { parseSpecFile, introspectSpec } from './specApi'

vi.mock('@/lib/apiClient', () => ({
  apiClient: {
    post: vi.fn(),
  },
}))

const mockPost = vi.mocked(apiClient.post)

beforeEach(() => {
  mockPost.mockReset()
})

describe('specApi', () => {
  it('parseSpecFile posts to /specs/parse with multipart', async () => {
    const payload = { title: 'Test', endpointCount: 3, endpoints: [] }
    mockPost.mockResolvedValueOnce({ data: payload })
    const file = new File(['{}'], 'spec.json', { type: 'application/json' })
    const result = await parseSpecFile(file)
    expect(mockPost).toHaveBeenCalledWith(
      '/specs/parse',
      expect.any(FormData),
      expect.objectContaining({ headers: { 'Content-Type': 'multipart/form-data' } }),
    )
    expect(result).toEqual(payload)
  })

  it('introspectSpec posts to /specs/introspect with encoded baseUrl', async () => {
    const payload = { title: 'Petstore', endpointCount: 10, endpoints: [] }
    mockPost.mockResolvedValueOnce({ data: payload })
    const result = await introspectSpec('https://petstore.swagger.io')
    expect(mockPost).toHaveBeenCalledWith(
      expect.stringContaining('petstore.swagger.io'),
    )
    expect(result).toEqual(payload)
  })

  it('introspectSpec encodes special characters in baseUrl', async () => {
    mockPost.mockResolvedValueOnce({ data: {} })
    await introspectSpec('http://api.example.com/v1?key=a b')
    const callArg = mockPost.mock.calls[0][0] as string
    expect(callArg).not.toContain(' ')
  })
})
