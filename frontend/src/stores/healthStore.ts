import { defineStore } from 'pinia'
import { getToolHealth } from '../api/toolApi'
import type { ToolHealth } from '../types/tool'

interface HealthState {
  loading: boolean
  health: ToolHealth | null
  traceId: string
  error: string
}

export const useHealthStore = defineStore('health', {
  state: (): HealthState => ({
    loading: false,
    health: null,
    traceId: '',
    error: '',
  }),
  actions: {
    async loadHealth() {
      this.loading = true
      this.error = ''
      try {
        const response = await getToolHealth()
        this.health = response.data
        this.traceId = response.traceId
      } catch (error) {
        this.error = error instanceof Error ? error.message : '未知错误'
      } finally {
        this.loading = false
      }
    },
  },
})
