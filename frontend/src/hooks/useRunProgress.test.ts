import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { renderHook } from '@testing-library/react'
import { useRunProgress } from './useRunProgress'
import { useRunStore } from '@/store/runStore'

// Minimal EventSource mock
class MockEventSource {
  static lastInstance: MockEventSource | null = null
  url: string
  onmessage: ((e: MessageEvent) => void) | null = null
  onerror: (() => void) | null = null
  close = vi.fn()

  constructor(url: string) {
    this.url = url
    MockEventSource.lastInstance = this
  }

  emit(data: string) {
    this.onmessage?.(new MessageEvent('message', { data }))
  }
}

beforeEach(() => {
  vi.stubGlobal('EventSource', MockEventSource)
  useRunStore.setState({ activeRunId: null, isRunning: false, events: [], history: [] })
})

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('useRunProgress', () => {
  it('opens EventSource for the given runId', () => {
    renderHook(() => useRunProgress('run-42'))
    expect(MockEventSource.lastInstance?.url).toBe('/api/runs/run-42/events')
  })

  it('appends events to the store', () => {
    renderHook(() => useRunProgress('run-42'))
    const event = JSON.stringify({
      runId: 'run-42',
      type: 'TEST_CASE_COMPLETED',
      payload: 'ok',
      timestamp: Date.now(),
    })
    MockEventSource.lastInstance?.emit(event)
    expect(useRunStore.getState().events).toHaveLength(1)
  })

  it('closes EventSource when runId is null', () => {
    const { rerender } = renderHook(
      ({ runId }: { runId: string | null }) => useRunProgress(runId as string),
      { initialProps: { runId: 'run-42' as string | null } },
    )
    const instance = MockEventSource.lastInstance!
    rerender({ runId: null })
    expect(instance.close).toHaveBeenCalled()
  })
})
