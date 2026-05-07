export interface RunTestsRequest {
  specUrl: string
  baseUrl: string
  testRunId: string
  projectId: string | null
  tenantId: string | null
  authToken: string | null
  config: RunConfig | null
}

export interface RunConfig {
  concurrency: number
  timeoutMs: number
}

export type ProgressEventType =
  | 'TEST_RUN_STARTED'
  | 'TEST_CASE_COMPLETED'
  | 'TEST_RUN_FINISHED'

export interface ProgressEvent {
  runId: string
  type: ProgressEventType
  payload: string
  timestamp: number
}

export interface TestCaseResult {
  testCaseId: string
  testCaseName: string
  passed: boolean
  statusCode: number
  responseTimeMs: number
  category: string
  violations: string[]
}

export interface ExecutionResult {
  runId: string
  totalTests: number
  passedTests: number
  failedTests: number
  durationMs: number
  results: TestCaseResult[]
}
