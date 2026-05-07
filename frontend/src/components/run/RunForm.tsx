import { useState } from 'react'
import type { RunTestsRequest } from '@/types/run'

interface Props {
  onSubmit: (req: RunTestsRequest) => void
  isRunning: boolean
}

const empty = { specUrl: '', baseUrl: '', authToken: '' }

export function RunForm({ onSubmit, isRunning }: Props) {
  const [form, setForm] = useState(empty)

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    if (!form.specUrl.trim() || !form.baseUrl.trim()) return
    onSubmit({
      specUrl: form.specUrl.trim(),
      baseUrl: form.baseUrl.trim(),
      testRunId: crypto.randomUUID(),
      projectId: null,
      tenantId: null,
      authToken: form.authToken.trim() || null,
      config: null,
    })
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <Field label="Spec URL *">
          <input
            required
            value={form.specUrl}
            onChange={(e) => setForm({ ...form, specUrl: e.target.value })}
            placeholder="https://petstore.swagger.io/v2/swagger.json"
            className="input"
          />
        </Field>
        <Field label="Base URL *">
          <input
            required
            value={form.baseUrl}
            onChange={(e) => setForm({ ...form, baseUrl: e.target.value })}
            placeholder="https://petstore.swagger.io"
            className="input"
          />
        </Field>
      </div>
      <Field label="Auth token (optional)">
        <input
          value={form.authToken}
          onChange={(e) => setForm({ ...form, authToken: e.target.value })}
          placeholder="Bearer token or API key"
          className="input"
        />
      </Field>
      <button
        type="submit"
        disabled={isRunning}
        className="px-6 py-2.5 bg-brand-600 hover:bg-brand-500 disabled:opacity-50 rounded-lg text-sm font-medium transition-colors"
      >
        {isRunning ? 'Running…' : 'Run Tests'}
      </button>
    </form>
  )
}

function Field({
  label,
  children,
}: {
  label: string
  children: React.ReactNode
}) {
  return (
    <div>
      <label className="block text-xs text-gray-400 mb-1">{label}</label>
      {children}
    </div>
  )
}
