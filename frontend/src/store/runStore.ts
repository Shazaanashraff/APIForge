import { create } from 'zustand'
import type { ProgressEvent, ExecutionResult } from '@/types/run'

export interface CompletedRun {
  runId: string
  result: ExecutionResult
  completedAt: string
}

interface RunState {
  activeRunId: string | null
  isRunning: boolean
  events: ProgressEvent[]
  history: CompletedRun[]
  startRun: (runId: string) => void
  appendEvent: (event: ProgressEvent) => void
  completeRun: (result: ExecutionResult) => void
  clearActive: () => void
}

export const useRunStore = create<RunState>((set, get) => ({
  activeRunId: null,
  isRunning: false,
  events: [],
  history: [],

  startRun: (runId) =>
    set({ activeRunId: runId, isRunning: true, events: [] }),

  appendEvent: (event) =>
    set({ events: [...get().events, event] }),

  completeRun: (result) =>
    set({
      isRunning: false,
      history: [
        { runId: result.runId, result, completedAt: new Date().toISOString() },
        ...get().history,
      ],
    }),

  clearActive: () =>
    set({ activeRunId: null, isRunning: false, events: [] }),
}))
