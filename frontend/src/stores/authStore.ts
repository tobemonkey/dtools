import { defineStore } from 'pinia'
import * as authApi from '../api/authApi'
import { ApiError, setAuthHandlers } from '../api/http'
import type { AuthTokenDTO, CurrentUserDTO, LoginCommand } from '../types/auth'
import {
  hasAllPermissions as checkAllPermissions,
  hasAnyPermission as checkAnyPermission,
  hasDataScope as checkDataScope,
  hasPermission as checkPermission,
} from '../utils/permission'

const ACCESS_TOKEN_KEY = 'dtools.auth.accessToken'
const REFRESH_TOKEN_KEY = 'dtools.auth.refreshToken'

interface AuthState {
  accessToken: string
  refreshToken: string
  currentUser: CurrentUserDTO | null
  loading: boolean
  refreshing: boolean
  initialized: boolean
  loginError: string
}

let refreshPromise: Promise<boolean> | null = null

function readSessionValue(key: string): string {
  return window.sessionStorage.getItem(key) ?? ''
}

function writeSessionTokens(accessToken: string, refreshToken: string) {
  // 阶段 1 仅用于开发闭环；后续 Web 化需要重新评估更安全的凭证存储策略。
  window.sessionStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
  window.sessionStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
}

function clearSessionTokens() {
  window.sessionStorage.removeItem(ACCESS_TOKEN_KEY)
  window.sessionStorage.removeItem(REFRESH_TOKEN_KEY)
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    accessToken: readSessionValue(ACCESS_TOKEN_KEY),
    refreshToken: readSessionValue(REFRESH_TOKEN_KEY),
    currentUser: null,
    loading: false,
    refreshing: false,
    initialized: false,
    loginError: '',
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.accessToken && state.currentUser),
  },
  actions: {
    applyToken(token: AuthTokenDTO) {
      this.accessToken = token.accessToken
      this.refreshToken = token.refreshToken
      this.currentUser = token.currentUser
      writeSessionTokens(token.accessToken, token.refreshToken)
    },
    clearSession() {
      this.accessToken = ''
      this.refreshToken = ''
      this.currentUser = null
      this.loginError = ''
      clearSessionTokens()
    },
    async login(command: LoginCommand): Promise<boolean> {
      this.loading = true
      this.loginError = ''
      try {
        const response = await authApi.login(command)
        this.applyToken(response.data)
        this.initialized = true
        return true
      } catch (error) {
        this.clearSession()
        this.initialized = true
        this.loginError = error instanceof Error ? error.message : '登录失败'
        return false
      } finally {
        this.loading = false
      }
    },
    async logout() {
      const refreshToken = this.refreshToken
      try {
        if (refreshToken) {
          await authApi.logout({ refreshToken })
        }
      } finally {
        this.clearSession()
        this.initialized = true
      }
    },
    async refresh(): Promise<boolean> {
      if (refreshPromise) {
        return refreshPromise
      }

      refreshPromise = this.doRefresh()
      try {
        return await refreshPromise
      } finally {
        refreshPromise = null
      }
    },
    async doRefresh(): Promise<boolean> {
      const refreshToken = this.refreshToken || readSessionValue(REFRESH_TOKEN_KEY)
      if (!refreshToken) {
        this.clearSession()
        this.initialized = true
        return false
      }

      this.refreshing = true
      try {
        const response = await authApi.refresh({ refreshToken })
        this.applyToken(response.data)
        this.initialized = true
        return true
      } catch {
        this.clearSession()
        this.initialized = true
        return false
      } finally {
        this.refreshing = false
      }
    },
    async ensureCurrentUser(): Promise<boolean> {
      if (this.currentUser && this.accessToken) {
        this.initialized = true
        return true
      }

      if (!this.accessToken && !this.refreshToken) {
        this.clearSession()
        this.initialized = true
        return false
      }

      this.loading = true
      try {
        if (!this.accessToken && this.refreshToken) {
          return await this.refresh()
        }

        const response = await authApi.getCurrentUser()
        this.currentUser = response.data
        this.initialized = true
        return true
      } catch (error) {
        if (error instanceof ApiError && error.status === 401) {
          const refreshed = await this.refresh()
          if (refreshed) {
            return true
          }
        }

        this.clearSession()
        this.initialized = true
        return false
      } finally {
        this.loading = false
      }
    },
    hasPermission(permission: string): boolean {
      return checkPermission(this.currentUser, permission)
    },
    hasAnyPermission(permissions: string[]): boolean {
      return checkAnyPermission(this.currentUser, permissions)
    },
    hasAllPermissions(permissions: string[]): boolean {
      return checkAllPermissions(this.currentUser, permissions)
    },
    hasDataScope(dataScope: string): boolean {
      return checkDataScope(this.currentUser, dataScope)
    },
  },
})

setAuthHandlers({
  getAccessToken: () => readSessionValue(ACCESS_TOKEN_KEY),
  refreshAccessToken: () => useAuthStore().refresh(),
  clearAuth: () => useAuthStore().clearSession(),
})
