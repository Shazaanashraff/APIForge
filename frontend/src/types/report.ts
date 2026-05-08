export interface CategoryStat {
  category: string
  passed: number
  failed: number
  total: number
}

export interface LatencyPoint {
  name: string
  responseTimeMs: number
  passed: boolean
}
