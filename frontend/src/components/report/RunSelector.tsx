import type { CompletedRun } from '@/store/runStore'

interface Props {
  runs: CompletedRun[]
  selectedId: string | null
  onChange: (id: string) => void
}

export function RunSelector({ runs, selectedId, onChange }: Props) {
  if (runs.length === 0) {
    return (
      <p className="text-sm text-gray-500">
        No completed runs yet. Go to{' '}
        <a href="/runs" className="text-brand-400 hover:underline">
          Test Runs
        </a>{' '}
        to execute a test suite.
      </p>
    )
  }

  return (
    <div className="flex items-center gap-3">
      <label className="text-sm text-gray-400 shrink-0">Viewing run:</label>
      <select
        value={selectedId ?? ''}
        onChange={(e) => onChange(e.target.value)}
        className="bg-gray-800 border border-gray-700 rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:border-brand-500"
      >
        {runs.map(({ runId, completedAt }) => (
          <option key={runId} value={runId}>
            {runId.slice(0, 8)}… — {new Date(completedAt).toLocaleString()}
          </option>
        ))}
      </select>
    </div>
  )
}
