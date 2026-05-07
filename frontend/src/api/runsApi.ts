import { apiClient } from '@/lib/apiClient'
import type { RunTestsRequest, ExecutionResult } from '@/types/run'

export async function triggerRun(request: RunTestsRequest): Promise<ExecutionResult> {
  const { data } = await apiClient.post<ExecutionResult>('/runs', request)
  return data
}
