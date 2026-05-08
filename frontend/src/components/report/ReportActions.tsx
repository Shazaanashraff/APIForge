import { downloadJson } from '@/utils/download'
import type { ExecutionResult } from '@/types/run'

interface Props {
  result: ExecutionResult
}

export function ReportActions({ result }: Props) {
  return (
    <div className="flex gap-2">
      <button
        onClick={() => downloadJson(result, `report-${result.runId.slice(0, 8)}.json`)}
        className="px-3 py-1.5 text-xs bg-gray-800 hover:bg-gray-700 border border-gray-700 rounded-lg transition-colors"
      >
        ↓ JSON
      </button>
    </div>
  )
}
