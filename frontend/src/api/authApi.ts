import type { ApiResponse } from '../types/api'
import type { AuthTokenDTO, CurrentUserDTO, LoginCommand, LogoutCommand, RefreshTokenCommand } from '../types/auth'
import { request } from './http'

export function login(command: LoginCommand): Promise<ApiResponse<AuthTokenDTO>> {
  return request<AuthTokenDTO>(
    '/api/auth/login',
    {
      method: 'POST',
      body: JSON.stringify(command),
    },
    { skipAuth: true },
  )
}

export function refresh(command: RefreshTokenCommand): Promise<ApiResponse<AuthTokenDTO>> {
  return request<AuthTokenDTO>(
    '/api/auth/refresh',
    {
      method: 'POST',
      body: JSON.stringify(command),
    },
    { skipAuth: true, skipRefresh: true },
  )
}

export function logout(command: LogoutCommand): Promise<ApiResponse<void>> {
  return request<void>('/api/auth/logout', {
    method: 'POST',
    body: JSON.stringify(command),
  })
}

export function getCurrentUser(): Promise<ApiResponse<CurrentUserDTO>> {
  return request<CurrentUserDTO>('/api/auth/me')
}
