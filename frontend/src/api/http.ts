import type { ApiResponse } from '../types/api'
import { getApiBaseUrl } from './apiBaseUrl'

interface RequestOptions {
  skipAuth?: boolean
  skipRefresh?: boolean
}

interface AuthHandlers {
  getAccessToken: () => string
  refreshAccessToken: () => Promise<boolean>
  clearAuth: () => void
}

export class ApiError extends Error {
  status: number
  code?: number
  traceId?: string

  constructor(payload: { status: number; code?: number; message: string; traceId?: string }) {
    super(payload.message)
    this.name = 'ApiError'
    this.status = payload.status
    this.code = payload.code
    this.traceId = payload.traceId
  }
}

let authHandlers: AuthHandlers | null = null

export function setAuthHandlers(handlers: AuthHandlers) {
  authHandlers = handlers
}

export async function request<T>(
  path: string,
  init?: RequestInit,
  options: RequestOptions = {},
): Promise<ApiResponse<T>> {
  return send<T>(path, init, options, false)
}

async function send<T>(
  path: string,
  init: RequestInit = {},
  options: RequestOptions,
  hasRetried: boolean,
): Promise<ApiResponse<T>> {
  const headers = new Headers(init.headers)
  if (!headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  const accessToken = options.skipAuth ? '' : authHandlers?.getAccessToken()
  if (accessToken) {
    headers.set('Authorization', `Bearer ${accessToken}`)
  }

  const response = await fetch(`${getApiBaseUrl()}${path}`, {
    ...init,
    headers,
  })

  if (response.status === 401 && !options.skipAuth && !options.skipRefresh && !hasRetried && authHandlers) {
    const refreshed = await authHandlers.refreshAccessToken()
    if (refreshed) {
      return send<T>(path, init, options, true)
    }

    authHandlers.clearAuth()
  }

  if (!response.ok) {
    throw await createApiError(response)
  }

  return response.json() as Promise<ApiResponse<T>>
}

async function createApiError(response: Response): Promise<ApiError> {
  try {
    const body = (await response.json()) as Partial<ApiResponse<unknown>>
    return new ApiError({
      status: response.status,
      code: typeof body.code === 'number' ? body.code : undefined,
      message: body.message || `HTTP ${response.status}`,
      traceId: body.traceId,
    })
  } catch {
    return new ApiError({
      status: response.status,
      message: `HTTP ${response.status}`,
    })
  }
}
