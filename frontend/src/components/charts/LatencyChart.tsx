import ReactECharts from 'echarts-for-react'
import type { LatencyPoint } from '@/types/report'

interface Props {
  points: LatencyPoint[]
  p95Ms: number
}

export function LatencyChart({ points, p95Ms }: Props) {
  if (points.length === 0) {
    return (
      <div className="flex items-center justify-center h-48 text-gray-500 text-sm">
        No data
      </div>
    )
  }

  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#1f2937',
      borderColor: '#374151',
      textStyle: { color: '#e5e7eb', fontSize: 12 },
      formatter: (params: { dataIndex: number; value: number }[]) => {
        const p = params[0]
        const name = points[p.dataIndex]?.name ?? ''
        return `${name}<br/>${p.value} ms`
      },
    },
    grid: { left: 50, right: 16, top: 16, bottom: 40 },
    xAxis: {
      type: 'category' as const,
      data: points.map((_, i) => String(i + 1)),
      axisLabel: { color: '#6b7280', fontSize: 10 },
      axisLine: { lineStyle: { color: '#374151' } },
    },
    yAxis: {
      type: 'value' as const,
      name: 'ms',
      nameTextStyle: { color: '#6b7280', fontSize: 11 },
      axisLabel: { color: '#6b7280', fontSize: 11 },
      splitLine: { lineStyle: { color: '#1f2937' } },
    },
    series: [
      {
        type: 'line' as const,
        data: points.map((p) => p.responseTimeMs),
        smooth: true,
        symbol: 'circle',
        symbolSize: 5,
        lineStyle: { color: '#818cf8', width: 2 },
        itemStyle: {
          color: (params: { dataIndex: number }) =>
            points[params.dataIndex]?.passed ? '#34d399' : '#f87171',
        },
        markLine: {
          silent: true,
          data: [{ yAxis: p95Ms, name: 'p95' }],
          lineStyle: { color: '#f59e0b', type: 'dashed' },
          label: { formatter: 'p95: {c}ms', color: '#f59e0b', fontSize: 11 },
        },
      },
    ],
  }

  return (
    <ReactECharts
      option={option}
      style={{ height: 260, width: '100%' }}
      theme="dark"
    />
  )
}
