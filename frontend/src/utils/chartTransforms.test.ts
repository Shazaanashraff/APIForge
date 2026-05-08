import { describe, it, expect } from 'vitest'
import { toCategoryStats, toLatencyPoints, p95 } from './chartTransforms'
import type { TestCaseResult } from '@/types/run'

const make = (
  category: string,
  passed: boolean,
  responseTimeMs = 100,
): TestCaseResult => ({
  testCaseId: crypto.randomUUID(),
  testCaseName: `${category}-test`,
  passed,
  statusCode: passed ? 200 : 500,
  responseTimeMs,
  category,
  violations: [],
})

describe('toCategoryStats', () => {
  it('groups results by category with pass/fail counts', () => {
    const results = [
      make('HAPPY_PATH', true),
      make('HAPPY_PATH', true),
      make('HAPPY_PATH', false),
      make('AUTH', false),
    ]
    const stats = toCategoryStats(results)
    const happy = stats.find((s) => s.category === 'HAPPY_PATH')!
    expect(happy.passed).toBe(2)
    expect(happy.failed).toBe(1)
    expect(happy.total).toBe(3)
  })

  it('returns empty array for empty input', () => {
    expect(toCategoryStats([])).toEqual([])
  })

  it('sorts by total descending', () => {
    const results = [make('B', true), make('A', true), make('A', false), make('A', true)]
    const stats = toCategoryStats(results)
    expect(stats[0].category).toBe('A')
  })
})

describe('toLatencyPoints', () => {
  it('maps results to latency points', () => {
    const r = make('BOUNDARY', true, 250)
    const points = toLatencyPoints([r])
    expect(points[0].responseTimeMs).toBe(250)
    expect(points[0].passed).toBe(true)
  })
})

describe('p95', () => {
  it('computes 95th percentile', () => {
    const points = Array.from({ length: 20 }, (_, i) =>
      ({ name: String(i), responseTimeMs: (i + 1) * 10, passed: true }),
    )
    expect(p95(points)).toBe(190)
  })

  it('returns 0 for empty array', () => {
    expect(p95([])).toBe(0)
  })
})
