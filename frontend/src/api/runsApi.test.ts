import { describe, it, expect, vi, beforeEach } from 'vitest'
import { apiClient } from '@/lib/apiClient'
import { triggerRun } from './runsApi'
import type { RunTestsRequest } from '@/types/run'

vi.mock('@/lib/apiClient', () => ({
  apiClient: { post: vi.fn() },
}))

const mockPost = vi.mocked(apiClient.post)

beforeEach(() => mockPost.mockReset())

const request: RunTestsRequest = {
  specUrl: 'http://spec',
  baseUrl: 'http://base',
  testRunId: 'run-1',
  projectId: null,
  tenantId: null,
  authToken: null,
  config: null,
}

describe('runsApi', () => {
  it('triggerRun posts to /runs', async () => {
    mockPost.mockResolvedValueOnce({ data: { runId: 'run-1', totalTests: 0 } })
    await triggerRun(request)
    expect(mockPost).toHaveBeenCalledWith('/runs', request)
  })

  it('triggerRun returns ExecutionResult from response', async () => {
    const payload = { runId: 'run-1', totalTests: 5, passedTests: 4, failedTests: 1 }
    mockPost.mockResolvedValueOnce({ data: payload })
    const result = await triggerRun(request)
    expect(result).toEqual(payload)
  })

  it('triggerRun propagates errors', async () => {
    mockPost.mockRejectedValueOnce(new Error('network error'))
    await expect(triggerRun(request)).rejects.toThrow('network error')
  })
})
