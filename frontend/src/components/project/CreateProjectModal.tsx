import { useState } from 'react'
import type { CreateProjectInput } from '@/types/project'

interface Props {
  onClose: () => void
  onCreate: (input: CreateProjectInput) => void
}

const empty: CreateProjectInput = {
  name: '',
  description: '',
  specUrl: '',
  mongoEnabled: false,
}

export function CreateProjectModal({ onClose, onCreate }: Props) {
  const [form, setForm] = useState<CreateProjectInput>(empty)

  function submit(e: React.FormEvent) {
    e.preventDefault()
    if (!form.name.trim()) return
    onCreate(form)
    onClose()
  }

  return (
    <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50 p-4">
      <div className="bg-gray-900 border border-gray-700 rounded-xl w-full max-w-md shadow-2xl">
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-800">
          <h2 className="font-semibold">New Project</h2>
          <button
            onClick={onClose}
            className="text-gray-500 hover:text-white text-xl leading-none"
          >
            ×
          </button>
        </div>
        <form onSubmit={submit} className="p-6 space-y-4">
          <Field label="Name *">
            <input
              required
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              placeholder="My API"
              className="w-full bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-brand-500"
            />
          </Field>
          <Field label="Description">
            <input
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
              placeholder="Optional description"
              className="w-full bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-brand-500"
            />
          </Field>
          <Field label="Spec URL">
            <input
              value={form.specUrl}
              onChange={(e) => setForm({ ...form, specUrl: e.target.value })}
              placeholder="https://…/openapi.json"
              className="w-full bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-brand-500"
            />
          </Field>
          <label className="flex items-center gap-3 cursor-pointer">
            <input
              type="checkbox"
              checked={form.mongoEnabled}
              onChange={(e) =>
                setForm({ ...form, mongoEnabled: e.target.checked })
              }
              className="accent-brand-500"
            />
            <span className="text-sm text-gray-300">Enable MongoDB-specific tests</span>
          </label>
          <div className="flex justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-sm text-gray-400 hover:text-white transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-brand-600 hover:bg-brand-500 rounded-lg text-sm font-medium transition-colors"
            >
              Create
            </button>
          </div>
        </form>
      </div>
    </div>
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
