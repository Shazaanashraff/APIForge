export interface Endpoint {
  method: string
  path: string
  operationId: string | null
  summary: string | null
  tags: string[]
}

export interface ParsedSpec {
  title: string
  specVersion: string
  sourceUrl: string | null
  parsedAt: string
  endpointCount: number
  endpoints: Endpoint[]
}

export interface IntrospectRequest {
  baseUrl: string
}
