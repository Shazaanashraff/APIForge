import { useRef, useState } from 'react'
import Editor from '@monaco-editor/react'
import { useMutation } from '@tanstack/react-query'
import { parseSpecFile, introspectSpec } from '@/api/specApi'
import { ParseResults } from '@/components/spec/ParseResults'
import type { ParsedSpec } from '@/types/spec'

export default function SpecsPage() {
  const fileRef = useRef<HTMLInputElement>(null)
  const [url, setUrl] = useState('')
  const [editorValue, setEditorValue] = useState('')
  const [result, setResult] = useState<ParsedSpec | null>(null)

  const parseMutation = useMutation({
    mutationFn: parseSpecFile,
    onSuccess: (data) => {
      setResult(data)
      setEditorValue(JSON.stringify(data, null, 2))
    },
  })

  const introspectMutation = useMutation({
    mutationFn: introspectSpec,
    onSuccess: (data) => {
      setResult(data)
      setEditorValue(JSON.stringify(data, null, 2))
    },
  })

  const isPending = parseMutation.isPending || introspectMutation.isPending
  const error = parseMutation.error ?? introspectMutation.error

  function onFileChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    if (file) parseMutation.mutate(file)
  }

  return (
    <div className="space-y-6 max-w-5xl">
      <div>
        <h1 className="text-2xl font-bold mb-1">API Specs</h1>
        <p className="text-gray-400 text-sm">
          Upload an OpenAPI file or introspect a live server to generate tests.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="bg-gray-900 border border-gray-800 rounded-xl p-5 space-y-3">
          <h2 className="font-medium text-sm text-gray-300">Upload spec file</h2>
          <input
            ref={fileRef}
            type="file"
            accept=".json,.yaml,.yml"
            className="hidden"
            onChange={onFileChange}
          />
          <button
            onClick={() => fileRef.current?.click()}
            disabled={isPending}
            className="w-full border-2 border-dashed border-gray-700 hover:border-brand-500 rounded-lg py-6 text-sm text-gray-400 hover:text-white transition-colors disabled:opacity-50"
          >
            {isPending && parseMutation.isPending
              ? 'Parsing…'
              : 'Click to choose .json / .yaml file'}
          </button>
        </div>

        <div className="bg-gray-900 border border-gray-800 rounded-xl p-5 space-y-3">
          <h2 className="font-medium text-sm text-gray-300">Introspect live server</h2>
          <input
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            placeholder="https://petstore.swagger.io"
            className="w-full bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-brand-500"
          />
          <button
            onClick={() => url.trim() && introspectMutation.mutate(url.trim())}
            disabled={isPending || !url.trim()}
            className="w-full bg-brand-600 hover:bg-brand-500 disabled:opacity-50 rounded-lg py-2 text-sm font-medium transition-colors"
          >
            {isPending && introspectMutation.isPending ? 'Introspecting…' : 'Introspect'}
          </button>
        </div>
      </div>

      {error && (
        <div className="bg-red-900/30 border border-red-700 rounded-lg px-4 py-3 text-sm text-red-300">
          {error instanceof Error ? error.message : 'Request failed'}
        </div>
      )}

      {result && (
        <div className="space-y-4">
          <h2 className="font-semibold">Parse Results</h2>
          <ParseResults spec={result} />
        </div>
      )}

      <div className="space-y-2">
        <h2 className="font-semibold text-sm text-gray-400">Response preview</h2>
        <div className="rounded-xl overflow-hidden border border-gray-800 h-64">
          <Editor
            height="100%"
            defaultLanguage="json"
            value={editorValue}
            onChange={(v) => setEditorValue(v ?? '')}
            theme="vs-dark"
            options={{
              minimap: { enabled: false },
              fontSize: 12,
              readOnly: !editorValue,
              scrollBeyondLastLine: false,
            }}
          />
        </div>
      </div>
    </div>
  )
}
