import { useAuth } from 'react-oidc-context'

export default function DashboardPage() {
  const auth = useAuth()
  const name =
    (auth.user?.profile.preferred_username as string | undefined) ??
    'there'

  return (
    <div className="max-w-3xl">
      <h1 className="text-2xl font-bold mb-1">Welcome back, {name}</h1>
      <p className="text-gray-400 text-sm mb-8">
        APIForge — automated API test generation and execution.
      </p>
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        {[
          { label: 'Projects', value: '—', color: 'bg-brand-600' },
          { label: 'Test Runs', value: '—', color: 'bg-emerald-600' },
          { label: 'Reports', value: '—', color: 'bg-amber-600' },
        ].map(({ label, value, color }) => (
          <div
            key={label}
            className="bg-gray-900 rounded-xl p-5 border border-gray-800"
          >
            <div
              className={`w-2 h-2 rounded-full ${color} mb-3`}
            />
            <p className="text-2xl font-bold">{value}</p>
            <p className="text-sm text-gray-400 mt-1">{label}</p>
          </div>
        ))}
      </div>
    </div>
  )
}
