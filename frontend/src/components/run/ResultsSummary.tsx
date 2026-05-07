import type { ExecutionResult } from '@/types/run'

interface Props {
  result: ExecutionResult
}

export function ResultsSummary({ result }: Props) {
  const passRate =
    result.totalTests > 0
      ? Math.round((result.passedTests / result.totalTests) * 100)
      : 0

  return (
    <div className="bg-gray-900 border border-gray-800 rounded-xl p-5 space-y-4">
      <h3 className="font-semibold">Results — {result.runId.slice(0, 8)}…</h3>
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        {[
          { label: 'Total', value: result.totalTests, color: 'text-white' },
          { label: 'Passed', value: result.passedTests, color: 'text-emerald-400' },
          { label: 'Failed', value: result.failedTests, color: 'text-red-400' },
          { label: 'Pass rate', value: `${passRate}%`, color: passRate >= 80 ? 'text-emerald-400' : 'text-amber-400' },
        ].map(({ label, value, color }) => (
          <div key={label} className="bg-gray-800 rounded-lg px-4 py-3 border border-gray-700">
            <p className="text-xs text-gray-400 mb-1">{label}</p>
            <p className={`text-xl font-bold ${color}`}>{value}</p>
          </div>
        ))}
      </div>
      {result.results.length > 0 && (
        <div className="overflow-x-auto rounded-lg border border-gray-700 max-h-64 overflow-y-auto">
          <table className="w-full text-xs">
            <thead className="bg-gray-800 text-gray-400 uppercase sticky top-0">
              <tr>
                <th className="px-3 py-2 text-left">Status</th>
                <th className="px-3 py-2 text-left">Name</th>
                <th className="px-3 py-2 text-left">Category</th>
                <th className="px-3 py-2 text-right">Time</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-800">
              {result.results.map((r) => (
                <tr key={r.testCaseId} className="hover:bg-gray-800/50">
                  <td className="px-3 py-1.5">
                    <span
                      className={`font-bold ${r.passed ? 'text-emerald-400' : 'text-red-400'}`}
                    >
                      {r.passed ? 'PASS' : 'FAIL'}
                    </span>
                  </td>
                  <td className="px-3 py-1.5 text-gray-300 max-w-xs truncate">
                    {r.testCaseName}
                  </td>
                  <td className="px-3 py-1.5 text-gray-500">{r.category}</td>
                  <td className="px-3 py-1.5 text-right text-gray-500">
                    {r.responseTimeMs}ms
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
