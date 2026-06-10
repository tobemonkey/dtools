import { defineStore } from 'pinia'
import {
  getApiMonitorErrors,
  getApiMonitorInterfaceDetail,
  getApiMonitorInterfaces,
  getApiMonitorOverview,
  getApiMonitorSlowEndpoints,
} from '../api/apiMonitorApi'
import type {
  ApiMonitorRangePreset,
  ApiMonitorTab,
  ApiOverview,
  ErrorSummary,
  InterfaceDetail,
  InterfaceSummary,
  SlowEndpoint,
} from '../types/apiMonitor'
import { createRangeByPreset } from '../utils/apiMonitorFormat'

interface ApiMonitorState {
  activeTab: ApiMonitorTab
  rangePreset: ApiMonitorRangePreset
  startTime: string
  endTime: string
  keyword: string
  loading: boolean
  detailLoading: boolean
  error: string
  traceId: string
  overview: ApiOverview | null
  slowEndpoints: SlowEndpoint[]
  errorSummary: ErrorSummary | null
  interfaces: InterfaceSummary[]
  selectedInterface: InterfaceSummary | null
  interfaceDetail: InterfaceDetail | null
}

const initialRange = createRangeByPreset('24h')

export const useApiMonitorStore = defineStore('apiMonitor', {
  state: (): ApiMonitorState => ({
    activeTab: 'overview',
    rangePreset: '24h',
    startTime: initialRange.startTime,
    endTime: initialRange.endTime,
    keyword: '',
    loading: false,
    detailLoading: false,
    error: '',
    traceId: '',
    overview: null,
    slowEndpoints: [],
    errorSummary: null,
    interfaces: [],
    selectedInterface: null,
    interfaceDetail: null,
  }),
  getters: {
    query: (state) => ({
      startTime: state.startTime,
      endTime: state.endTime,
    }),
    hasRange: (state) => Boolean(state.startTime && state.endTime),
  },
  actions: {
    setTab(tab: ApiMonitorTab) {
      this.activeTab = tab
      void this.loadActiveTab()
    },
    setRangePreset(preset: ApiMonitorRangePreset) {
      this.rangePreset = preset
      if (preset !== 'custom') {
        const range = createRangeByPreset(preset)
        this.startTime = range.startTime
        this.endTime = range.endTime
      }
      this.clearData()
    },
    setCustomRange(startTime: string, endTime: string) {
      this.rangePreset = 'custom'
      this.startTime = startTime
      this.endTime = endTime
      this.clearData()
    },
    async refreshAll() {
      await this.loadActiveTab(true)
    },
    async loadActiveTab(force = false) {
      if (!this.hasRange) {
        this.error = '请选择统计时间范围'
        return
      }

      if (this.activeTab === 'overview') {
        await this.loadOverview(force)
      } else if (this.activeTab === 'slow') {
        await this.loadSlowEndpoints(force)
      } else if (this.activeTab === 'errors') {
        await this.loadErrors(force)
      } else {
        await this.loadInterfaces(force)
      }
    },
    async loadOverview(force = false) {
      if (!force && this.overview) return
      await this.withLoading(async () => {
        const response = await getApiMonitorOverview(this.query)
        this.overview = response.data
        this.traceId = response.traceId
      })
    },
    async loadSlowEndpoints(force = false) {
      if (!force && this.slowEndpoints.length > 0) return
      await this.withLoading(async () => {
        const response = await getApiMonitorSlowEndpoints(this.query)
        this.slowEndpoints = response.data
        this.traceId = response.traceId
      })
    },
    async loadErrors(force = false) {
      if (!force && this.errorSummary) return
      await this.withLoading(async () => {
        const response = await getApiMonitorErrors(this.query)
        this.errorSummary = response.data
        this.traceId = response.traceId
      })
    },
    async loadInterfaces(force = false) {
      if (!force && this.interfaces.length > 0) return
      await this.withLoading(async () => {
        const response = await getApiMonitorInterfaces({
          ...this.query,
          keyword: this.keyword.trim() || undefined,
        })
        this.interfaces = response.data
        this.traceId = response.traceId
        if (!this.selectedInterface && this.interfaces.length > 0) {
          await this.selectInterface(this.interfaces[0])
        }
      })
    },
    async searchInterfaces() {
      this.selectedInterface = null
      this.interfaceDetail = null
      await this.loadInterfaces(true)
    },
    async selectInterface(item: InterfaceSummary) {
      this.selectedInterface = item
      this.interfaceDetail = null
      this.detailLoading = true
      this.error = ''
      try {
        const response = await getApiMonitorInterfaceDetail({
          ...this.query,
          method: item.method,
          uri: item.uri,
        })
        this.interfaceDetail = response.data
        this.traceId = response.traceId
      } catch (error) {
        this.error = error instanceof Error ? error.message : '接口明细加载失败'
      } finally {
        this.detailLoading = false
      }
    },
    async withLoading(action: () => Promise<void>) {
      this.loading = true
      this.error = ''
      try {
        await action()
      } catch (error) {
        this.error = error instanceof Error ? error.message : '接口监控数据加载失败'
      } finally {
        this.loading = false
      }
    },
    clearData() {
      this.overview = null
      this.slowEndpoints = []
      this.errorSummary = null
      this.interfaces = []
      this.selectedInterface = null
      this.interfaceDetail = null
      this.traceId = ''
      this.error = ''
    },
  },
})
