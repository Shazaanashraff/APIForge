import { useEffect } from 'react'
import { useRunStore } from '@/store/runStore'
import type { ProgressEvent } from '@/types/run'

export function useRunProgress(runId: string | null) {
  const { appendEvent, completeRun } = useRunStore()

  useEffect(() => {
    if (!runId) return
    const es = new EventSource(`/api/runs/${runId}/events`)

    es.onmessage = (e: MessageEvent<string>) => {
      try {
        const event: ProgressEvent = JSON.parse(e.data)
        appendEvent(event)
        if (event.type === 'TEST_RUN_FINISHED') {
          es.close()
        }
      } catch {
        // ignore malformed frames
      }
    }

    es.onerror = () => es.close()

    return () => es.close()
  }, [runId, appendEvent, completeRun])
}
