import { describe, it, expect, beforeEach } from 'vitest'
import { useRunStore } from './runStore'
import type { ExecutionResult } from '@/types/run'

beforeEach(() => {
  useRunStore.setState({
    activeRunId: null,
    isRunning: false,
    events: [],
    history: [],
  })
})

const mockResult: ExecutionResult = {
  runId: 'run-123',
  totalTests: 10,
  passedTests: 8,
  failedTests: 2,
  durationMs: 1500,
  results: [],
}

describe('runStore', () => {
  it('startRun sets activeRunId and clears events', () => {
    useRunStore.getState().startRun('abc')
    const { activeRunId, isRunning, events } = useRunStore.getState()
    expect(activeRunId).toBe('abc')
    expect(isRunning).toBe(true)
    expect(events).toHaveLength(0)
  })

  it('appendEvent adds to events array', () => {
    useRunStore.getState().appendEvent({
      runId: 'abc',
      type: 'TEST_CASE_COMPLETED',
      payload: 'ok',
      timestamp: Date.now(),
    })
    expect(useRunStore.getState().events).toHaveLength(1)
  })

  it('completeRun stops running and pushes to history', () => {
    useRunStore.getState().startRun('run-123')
    useRunStore.getState().completeRun(mockResult)
    const { isRunning, history } = useRunStore.getState()
    expect(isRunning).toBe(false)
    expect(history).toHaveLength(1)
    expect(history[0].runId).toBe('run-123')
  })
})
