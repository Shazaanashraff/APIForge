import type { ProgressEvent } from '@/types/run'

interface Props {
  events: ProgressEvent[]
  isRunning: boolean
}

const typeLabel: Record<string, string> = {
  TEST_RUN_STARTED: '▶ Run started',
  TEST_CASE_COMPLETED: '✓ Case done',
  TEST_RUN_FINISHED: '■ Run finished',
}

const typeColor: Record<string, string> = {
  TEST_RUN_STARTED: 'text-blue-400',
  TEST_CASE_COMPLETED: 'text-emerald-400',
  TEST_RUN_FINISHED: 'text-brand-400',
}

export function ProgressPanel({ events, isRunning }: Props) {
  const completed = events.filter(
    (e) => e.type === 'TEST_CASE_COMPLETED',
  ).length

  return (
    <div className="bg-gray-900 border border-gray-800 rounded-xl overflow-hidden">
      <div className="flex items-center justify-between px-4 py-3 border-b border-gray-800">
        <span className="text-sm font-medium">Progress</span>
        <div className="flex items-center gap-2">
          {isRunning && (
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
          )}
          <span className="text-xs text-gray-400">{completed} cases processed</span>
        </div>
      </div>
      <div className="h-64 overflow-y-auto p-3 space-y-1 font-mono text-xs">
        {events.length === 0 ? (
          <p className="text-gray-600 py-4 text-center">
            {isRunning ? 'Waiting for events…' : 'No run active.'}
          </p>
        ) : (
          events.map((ev, i) => (
            <div key={i} className="flex gap-3">
              <span className="text-gray-600 shrink-0">
                {new Date(ev.timestamp).toLocaleTimeString()}
              </span>
              <span className={typeColor[ev.type] ?? 'text-gray-400'}>
                {typeLabel[ev.type] ?? ev.type}
              </span>
              {ev.payload && (
                <span className="text-gray-400 truncate">{ev.payload}</span>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  )
}
