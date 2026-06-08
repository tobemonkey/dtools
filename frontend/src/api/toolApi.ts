import type { ApiResponse } from '../types/api'
import type { ToolHealth } from '../types/tool'
import { request } from './http'

export function getToolHealth(): Promise<ApiResponse<ToolHealth>> {
  return request<ToolHealth>('/api/health')
}
