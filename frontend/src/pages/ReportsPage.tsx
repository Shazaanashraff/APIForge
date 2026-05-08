import { useState } from 'react'
import { useRunStore } from '@/store/runStore'
import { toCategoryStats, toLatencyPoints, p95 } from '@/utils/chartTransforms'
import { CategoryBarChart } from '@/components/charts/CategoryBarChart'
import { LatencyChart } from '@/components/charts/LatencyChart'
import { RunSelector } from '@/components/report/RunSelector'
import { ReportActions } from '@/components/report/ReportActions'

export default function ReportsPage() {
  const history = useRunStore((s) => s.history)
  const [selectedId, setSelectedId] = useState<string | null>(
    history[0]?.runId ?? null,
  )

  const run = history.find((r) => r.runId === selectedId) ?? history[0] ?? null
  const results = run?.result.results ?? []
  const categoryStats = toCategoryStats(results)
  const latencyPoints = toLatencyPoints(results)
  const p95Ms = p95(latencyPoints)

  return (
    <div className="space-y-6 max-w-5xl">
      <div>
        <h1 className="text-2xl font-bold mb-1">Reports</h1>
        <p className="text-gray-400 text-sm">
          Visualize test results by category and response latency.
        </p>
      </div>

      <div className="flex items-center justify-between gap-4 flex-wrap">
        <RunSelector
          runs={history}
          selectedId={selectedId}
          onChange={setSelectedId}
        />
        {run && <ReportActions result={run.result} />}
      </div>

      {run ? (
        <>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
            {[
              { label: 'Total', value: run.result.totalTests, color: 'text-white' },
              { label: 'Passed', value: run.result.passedTests, color: 'text-emerald-400' },
              { label: 'Failed', value: run.result.failedTests, color: 'text-red-400' },
              {
                label: 'Duration',
                value: `${(run.result.durationMs / 1000).toFixed(1)}s`,
                color: 'text-brand-400',
              },
            ].map(({ label, value, color }) => (
              <div
                key={label}
                className="bg-gray-900 border border-gray-800 rounded-xl px-4 py-3"
              >
                <p className="text-xs text-gray-400 mb-1">{label}</p>
                <p className={`text-xl font-bold ${color}`}>{value}</p>
              </div>
            ))}
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <div className="bg-gray-900 border border-gray-800 rounded-xl p-5">
              <h2 className="font-medium text-sm mb-4">Pass / Fail by category</h2>
              <CategoryBarChart data={categoryStats} />
            </div>
            <div className="bg-gray-900 border border-gray-800 rounded-xl p-5">
              <div className="flex items-center justify-between mb-4">
                <h2 className="font-medium text-sm">Response latency</h2>
                {latencyPoints.length > 0 && (
                  <span className="text-xs text-amber-400">p95: {p95Ms}ms</span>
                )}
              </div>
              <LatencyChart points={latencyPoints} p95Ms={p95Ms} />
            </div>
          </div>
        </>
      ) : (
        <div className="py-16 text-center text-gray-500 text-sm">
          Complete a test run first to see reports here.
        </div>
      )}
    </div>
  )
}
