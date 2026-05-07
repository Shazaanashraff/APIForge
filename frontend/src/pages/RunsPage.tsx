import { useMutation } from '@tanstack/react-query'
import { triggerRun } from '@/api/runsApi'
import { useRunStore } from '@/store/runStore'
import { useRunProgress } from '@/hooks/useRunProgress'
import { RunForm } from '@/components/run/RunForm'
import { ProgressPanel } from '@/components/run/ProgressPanel'
import { ResultsSummary } from '@/components/run/ResultsSummary'
import type { RunTestsRequest } from '@/types/run'

export default function RunsPage() {
  const { activeRunId, isRunning, events, history, startRun, completeRun } =
    useRunStore()

  useRunProgress(activeRunId)

  const runMutation = useMutation({
    mutationFn: (req: RunTestsRequest) => triggerRun(req),
    onMutate: (req) => startRun(req.testRunId),
    onSuccess: (result) => completeRun(result),
    onError: () => useRunStore.getState().clearActive(),
  })

  return (
    <div className="space-y-6 max-w-4xl">
      <div>
        <h1 className="text-2xl font-bold mb-1">Test Runs</h1>
        <p className="text-gray-400 text-sm">
          Trigger a full test pipeline and watch results stream in real time.
        </p>
      </div>

      <div className="bg-gray-900 border border-gray-800 rounded-xl p-5">
        <h2 className="font-medium mb-4">Configure run</h2>
        <RunForm
          isRunning={isRunning || runMutation.isPending}
          onSubmit={(req) => runMutation.mutate(req)}
        />
        {runMutation.error && (
          <div className="mt-3 text-sm text-red-400 bg-red-900/20 border border-red-800 rounded-lg px-4 py-2">
            {runMutation.error instanceof Error
              ? runMutation.error.message
              : 'Run failed'}
          </div>
        )}
      </div>

      {(isRunning || events.length > 0) && (
        <ProgressPanel events={events} isRunning={isRunning} />
      )}

      {history.length > 0 && (
        <div className="space-y-4">
          <h2 className="font-semibold">Past runs</h2>
          {history.map(({ runId, result, completedAt }) => (
            <div key={runId}>
              <p className="text-xs text-gray-500 mb-2">
                Completed {new Date(completedAt).toLocaleString()}
              </p>
              <ResultsSummary result={result} />
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
