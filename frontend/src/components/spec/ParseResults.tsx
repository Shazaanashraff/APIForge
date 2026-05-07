import type { ParsedSpec } from '@/types/spec'

interface Props {
  spec: ParsedSpec
}

export function ParseResults({ spec }: Props) {
  return (
    <div className="space-y-4">
      <div className="grid grid-cols-3 gap-3">
        {[
          { label: 'Title', value: spec.title },
          { label: 'Version', value: spec.specVersion },
          { label: 'Endpoints', value: String(spec.endpointCount) },
        ].map(({ label, value }) => (
          <div
            key={label}
            className="bg-gray-800 rounded-lg px-4 py-3 border border-gray-700"
          >
            <p className="text-xs text-gray-400 mb-1">{label}</p>
            <p className="text-sm font-semibold truncate">{value}</p>
          </div>
        ))}
      </div>
      <div className="overflow-x-auto rounded-lg border border-gray-700">
        <table className="w-full text-sm">
          <thead className="bg-gray-800 text-gray-400 uppercase text-xs">
            <tr>
              <th className="px-4 py-2 text-left w-20">Method</th>
              <th className="px-4 py-2 text-left">Path</th>
              <th className="px-4 py-2 text-left">Summary</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-800">
            {spec.endpoints.map((ep, i) => (
              <tr key={i} className="hover:bg-gray-800/50 transition-colors">
                <td className="px-4 py-2">
                  <MethodBadge method={ep.method} />
                </td>
                <td className="px-4 py-2 font-mono text-xs text-gray-300">
                  {ep.path}
                </td>
                <td className="px-4 py-2 text-gray-400 text-xs">
                  {ep.summary ?? ep.operationId ?? '—'}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

function MethodBadge({ method }: { method: string }) {
  const colors: Record<string, string> = {
    GET: 'text-emerald-400 bg-emerald-400/10',
    POST: 'text-blue-400 bg-blue-400/10',
    PUT: 'text-amber-400 bg-amber-400/10',
    PATCH: 'text-orange-400 bg-orange-400/10',
    DELETE: 'text-red-400 bg-red-400/10',
  }
  const cls = colors[method.toUpperCase()] ?? 'text-gray-400 bg-gray-400/10'
  return (
    <span className={`px-2 py-0.5 rounded text-xs font-bold uppercase ${cls}`}>
      {method}
    </span>
  )
}
