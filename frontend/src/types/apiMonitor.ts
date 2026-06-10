export type ApiMonitorTab = 'overview' | 'slow' | 'errors' | 'interfaces'

export type ApiMonitorRangePreset = '5min' | '1h' | '24h' | 'custom'

export interface ApiMonitorQuery {
  startTime: string
  endTime: string
}

export interface ApiMonitorSearchQuery extends ApiMonitorQuery {
  keyword?: string
}

export interface ApiMonitorDetailQuery extends ApiMonitorQuery {
  method: string
  uri: string
}

export interface TrendPoint {
  bucketTime: string
  totalCount: number
  errorCount?: number
  avgCostMs?: number
}

export interface StatusItem {
  status: string
  count: number
}

export interface ErrorTypeItem {
  errorType: string
  count: number
}

export interface RecentErrorRequest {
  traceId: string
  method: string
  uri: string
  httpStatus?: number
  responseCode?: number
  costMs?: number
  exceptionType?: string
  exceptionMessage?: string
  createdAt: string
}

export interface ApiOverview {
  totalCount: number
  successCount: number
  errorCount: number
  errorRate: number
  avgCostMs: number
  t95CostMs: number
  t99CostMs: number
  maxCostMs: number
  slowestMethod?: string
  slowestUri?: string
  slowestCostMs?: number
  trendPoints: TrendPoint[]
  statusSegments: StatusItem[]
}

export interface SlowEndpoint {
  method: string
  uri: string
  requestCount: number
  avgCostMs: number
  t90CostMs: number
  t95CostMs: number
  t99CostMs: number
  maxCostMs: number
  timeoutRate: number
}

export interface ErrorSummary {
  errorTypeItems: ErrorTypeItem[]
  statusItems: StatusItem[]
  recentErrors: RecentErrorRequest[]
}

export interface InterfaceSummary {
  method: string
  uri: string
  requestCount: number
  errorCount: number
  errorRate: number
  avgCostMs: number
  t95CostMs: number
  t99CostMs: number
  maxCostMs: number
  latestTraceId?: string
}

export interface LatencyBucket {
  bucketName: string
  count: number
}

export interface InterfaceDetail {
  summary: InterfaceSummary
  trendPoints: TrendPoint[]
  statusItems: StatusItem[]
  errorTypeItems: ErrorTypeItem[]
  latencyBuckets: LatencyBucket[]
  recentErrors: RecentErrorRequest[]
}
