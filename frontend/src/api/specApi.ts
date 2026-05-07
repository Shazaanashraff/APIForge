import { apiClient } from '@/lib/apiClient'
import type { ParsedSpec } from '@/types/spec'

export async function parseSpecFile(file: File): Promise<ParsedSpec> {
  const form = new FormData()
  form.append('file', file)
  const { data } = await apiClient.post<ParsedSpec>('/specs/parse', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return data
}

export async function introspectSpec(baseUrl: string): Promise<ParsedSpec> {
  const { data } = await apiClient.post<ParsedSpec>(
    `/specs/introspect?baseUrl=${encodeURIComponent(baseUrl)}`,
  )
  return data
}
