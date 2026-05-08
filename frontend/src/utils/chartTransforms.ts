import type { TestCaseResult } from '@/types/run'
import type { CategoryStat, LatencyPoint } from '@/types/report'

export function toCategoryStats(results: TestCaseResult[]): CategoryStat[] {
  const map = new Map<string, { passed: number; failed: number }>()
  for (const r of results) {
    const entry = map.get(r.category) ?? { passed: 0, failed: 0 }
    if (r.passed) entry.passed += 1
    else entry.failed += 1
    map.set(r.category, entry)
  }
  return Array.from(map.entries())
    .map(([category, { passed, failed }]) => ({
      category,
      passed,
      failed,
      total: passed + failed,
    }))
    .sort((a, b) => b.total - a.total)
}

export function toLatencyPoints(results: TestCaseResult[]): LatencyPoint[] {
  return results.map((r) => ({
    name: r.testCaseName,
    responseTimeMs: r.responseTimeMs,
    passed: r.passed,
  }))
}

export function p95(points: LatencyPoint[]): number {
  if (points.length === 0) return 0
  const sorted = [...points].sort((a, b) => a.responseTimeMs - b.responseTimeMs)
  const idx = Math.ceil(0.95 * sorted.length) - 1
  return sorted[Math.max(0, idx)].responseTimeMs
}
