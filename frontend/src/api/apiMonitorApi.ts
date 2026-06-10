import type { ApiResponse } from '../types/api'
import type {
  ApiMonitorDetailQuery,
  ApiMonitorQuery,
  ApiMonitorSearchQuery,
  ApiOverview,
  ErrorSummary,
  InterfaceDetail,
  InterfaceSummary,
  SlowEndpoint,
} from '../types/apiMonitor'
import { request } from './http'

export function getApiMonitorOverview(query: ApiMonitorQuery): Promise<ApiResponse<ApiOverview>> {
  return request<ApiOverview>(`/api/monitor/api/overview?${toSearchParams(query)}`)
}

export function getApiMonitorSlowEndpoints(query: ApiMonitorQuery): Promise<ApiResponse<SlowEndpoint[]>> {
  return request<SlowEndpoint[]>(`/api/monitor/api/slow?${toSearchParams(query)}`)
}

export function getApiMonitorErrors(query: ApiMonitorQuery): Promise<ApiResponse<ErrorSummary>> {
  return request<ErrorSummary>(`/api/monitor/api/errors?${toSearchParams(query)}`)
}

export function getApiMonitorInterfaces(query: ApiMonitorSearchQuery): Promise<ApiResponse<InterfaceSummary[]>> {
  return request<InterfaceSummary[]>(`/api/monitor/api/interfaces?${toSearchParams(query)}`)
}

export function getApiMonitorInterfaceDetail(query: ApiMonitorDetailQuery): Promise<ApiResponse<InterfaceDetail>> {
  return request<InterfaceDetail>(`/api/monitor/api/interfaces/detail?${toSearchParams(query)}`)
}

function toSearchParams<T extends object>(query: T): string {
  const params = new URLSearchParams()
  Object.entries(query).forEach(([key, value]) => {
    if (typeof value === 'string' && value) {
      params.set(key, value)
    }
  })
  return params.toString()
}
