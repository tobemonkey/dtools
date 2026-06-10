import type { ApiMonitorRangePreset, ErrorTypeItem, LatencyBucket, StatusItem, TrendPoint } from '../types/apiMonitor'

export function createRangeByPreset(preset: Exclude<ApiMonitorRangePreset, 'custom'>, baseDate = new Date()) {
  const end = new Date(baseDate)
  const start = new Date(end)
  const minutes = preset === '5min' ? 5 : preset === '1h' ? 60 : 24 * 60
  start.setMinutes(start.getMinutes() - minutes)

  return {
    startTime: toLocalDateTimeInput(start),
    endTime: toLocalDateTimeInput(end),
  }
}

export function toLocalDateTimeInput(date: Date): string {
  const year = date.getFullYear()
  const month = pad(date.getMonth() + 1)
  const day = pad(date.getDate())
  const hours = pad(date.getHours())
  const minutes = pad(date.getMinutes())
  return `${year}-${month}-${day}T${hours}:${minutes}`
}

export function formatNumber(value: number | null | undefined): string {
  if (typeof value !== 'number' || Number.isNaN(value)) {
    return '-'
  }
  return new Intl.NumberFormat('zh-CN').format(value)
}

export function formatDuration(value: number | null | undefined): string {
  if (typeof value !== 'number' || Number.isNaN(value)) {
    return '-'
  }
  if (value >= 1000) {
    return `${(value / 1000).toFixed(value >= 10000 ? 1 : 2)}s`
  }
  return `${Math.round(value)}ms`
}

export function formatRate(value: number | null | undefined): string {
  if (typeof value !== 'number' || Number.isNaN(value)) {
    return '-'
  }
  const normalized = value > 1 ? value : value * 100
  return `${normalized.toFixed(normalized >= 10 ? 1 : 2)}%`
}

export function methodTone(method: string): string {
  const normalized = method.toUpperCase()
  if (normalized === 'GET') return 'get'
  if (normalized === 'POST') return 'post'
  if (normalized === 'PUT' || normalized === 'PATCH') return 'write'
  if (normalized === 'DELETE') return 'delete'
  return 'default'
}

export type MonitorDistributionItem = StatusItem | ErrorTypeItem | LatencyBucket

export function distributionTotal(items: MonitorDistributionItem[]): number {
  return items.reduce((total, item) => total + item.count, 0)
}

export function distributionLabel(item: MonitorDistributionItem): string {
  if ('status' in item) {
    return item.status
  }
  if ('errorType' in item) {
    return item.errorType
  }
  return item.bucketName
}

export function successRateFromErrorRate(errorRate: number): number {
  return Math.max(0, 1 - errorRate)
}

export function trendToPolyline(
  points: TrendPoint[],
  width: number,
  height: number,
  valueKey: keyof TrendPoint = 'totalCount',
): string {
  if (points.length === 0) {
    return ''
  }

  const values = points.map((point) => Number(point[valueKey]) || 0)
  const max = Math.max(...values, 1)
  const step = points.length > 1 ? width / (points.length - 1) : width

  return values
    .map((value, index) => {
      const x = points.length > 1 ? index * step : width / 2
      const y = height - (value / max) * height
      return `${x.toFixed(2)},${y.toFixed(2)}`
    })
    .join(' ')
}

function pad(value: number): string {
  return value.toString().padStart(2, '0')
}
