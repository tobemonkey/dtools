<template>
  <main class="workspace api-monitor-page">
    <div class="home-background" aria-hidden="true">
      <span class="bubble bubble-cyan"></span>
      <span class="bubble bubble-gold"></span>
      <span class="bubble bubble-mint"></span>
    </div>

    <section class="api-monitor-shell animate-card" style="--order: 1">
      <header class="api-monitor-hero">
        <button class="back-button" type="button" @click="router.push('/')">
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M15 18 9 12l6-6" />
          </svg>
          首页
        </button>
        <div>
          <p class="eyebrow">API Monitor</p>
          <h1>接口监控</h1>
          <p>从请求量、长尾耗时、异常链路和单接口明细观察后端接口健康状态。</p>
        </div>
        <button class="refresh-button" type="button" :disabled="store.loading" @click="store.refreshAll()">
          {{ store.loading ? '刷新中...' : '刷新' }}
        </button>
      </header>

      <section class="filter-bar">
        <div class="range-tabs" aria-label="快捷时间范围">
          <button
            v-for="preset in presets"
            :key="preset.value"
            type="button"
            :class="{ active: store.rangePreset === preset.value }"
            @click="handlePreset(preset.value)"
          >
            {{ preset.label }}
          </button>
        </div>
        <label>
          <span>开始</span>
          <input v-model="startInput" type="datetime-local" @change="handleCustomRange" />
        </label>
        <label>
          <span>结束</span>
          <input v-model="endInput" type="datetime-local" @change="handleCustomRange" />
        </label>
      </section>

      <nav class="monitor-tabs" aria-label="接口监控分类">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          type="button"
          :class="{ active: store.activeTab === tab.value }"
          @click="store.setTab(tab.value)"
        >
          <span>{{ tab.label }}</span>
          <small>{{ tab.desc }}</small>
        </button>
      </nav>

      <div v-if="store.error" class="monitor-state monitor-state--error">
        <strong>加载失败</strong>
        <span>{{ store.error }}</span>
        <small v-if="store.traceId">TraceID: {{ store.traceId }}</small>
      </div>
      <div v-else-if="store.loading" class="monitor-state">接口监控数据加载中...</div>

      <OverviewTab v-if="store.activeTab === 'overview'" :overview="store.overview" />
      <SlowEndpointsTab v-else-if="store.activeTab === 'slow'" :endpoints="store.slowEndpoints" />
      <ErrorRequestsTab v-else-if="store.activeTab === 'errors'" :summary="store.errorSummary" />
      <InterfaceDetailsTab
        v-else
        v-model:keyword="store.keyword"
        :interfaces="store.interfaces"
        :selected-interface="store.selectedInterface"
        :detail="store.interfaceDetail"
        :detail-loading="store.detailLoading"
        @search="store.searchInterfaces()"
        @select="store.selectInterface"
      />
    </section>
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import ErrorRequestsTab from '../components/api-monitor/ErrorRequestsTab.vue'
import InterfaceDetailsTab from '../components/api-monitor/InterfaceDetailsTab.vue'
import OverviewTab from '../components/api-monitor/OverviewTab.vue'
import SlowEndpointsTab from '../components/api-monitor/SlowEndpointsTab.vue'
import { useApiMonitorStore } from '../stores/apiMonitorStore'
import type { ApiMonitorRangePreset, ApiMonitorTab } from '../types/apiMonitor'

const router = useRouter()
const store = useApiMonitorStore()

const startInput = ref(store.startTime)
const endInput = ref(store.endTime)

const presets: Array<{ label: string; value: ApiMonitorRangePreset }> = [
  { label: '5min', value: '5min' },
  { label: '1h', value: '1h' },
  { label: '24h', value: '24h' },
]

const tabs: Array<{ label: string; value: ApiMonitorTab; desc: string }> = [
  { label: '总体统计', value: 'overview', desc: '请求量与状态' },
  { label: '慢接口', value: 'slow', desc: '长尾耗时' },
  { label: '异常请求', value: 'errors', desc: 'TraceID 排查' },
  { label: '接口明细', value: 'interfaces', desc: '单接口画像' },
]

function handlePreset(preset: ApiMonitorRangePreset) {
  store.setRangePreset(preset)
  startInput.value = store.startTime
  endInput.value = store.endTime
  void store.refreshAll()
}

function handleCustomRange() {
  store.setCustomRange(startInput.value, endInput.value)
  void store.refreshAll()
}

watch(
  () => [store.startTime, store.endTime],
  ([startTime, endTime]) => {
    startInput.value = startTime
    endInput.value = endTime
  },
)

onMounted(() => {
  void store.loadActiveTab()
})
</script>

<style scoped>
.api-monitor-page {
  padding-bottom: 42px;
}

.api-monitor-shell {
  position: relative;
  z-index: 1;
  width: min(1220px, calc(100vw - 44px));
  display: grid;
  gap: 20px;
  margin: 28px auto;
}

.api-monitor-hero,
.filter-bar,
.monitor-tabs,
.monitor-state {
  border: 1px solid rgba(255, 255, 255, 0.82);
  background: rgba(255, 255, 255, 0.58);
  box-shadow:
    0 34px 56px -38px rgba(55, 68, 78, 0.22),
    inset 0 0 20px rgba(255, 255, 255, 0.22);
  backdrop-filter: blur(18px);
}

.api-monitor-hero {
  min-height: 176px;
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 20px;
  align-items: center;
  border-radius: 38px;
  padding: 26px;
}

.api-monitor-hero h1 {
  margin: 0;
  color: #334f52;
  font-size: clamp(38px, 7vw, 74px);
  line-height: 0.95;
  letter-spacing: 0;
}

.api-monitor-hero p:last-child {
  max-width: 650px;
  margin: 14px 0 0;
  color: rgba(31, 35, 41, 0.58);
  line-height: 1.7;
}

.back-button,
.refresh-button {
  height: 48px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-radius: 18px;
  padding: 0 16px;
  font-weight: 850;
}

.back-button {
  color: #334f52;
  background: rgba(255, 255, 255, 0.62);
}

.back-button svg {
  width: 18px;
  height: 18px;
  fill: none;
  stroke: currentColor;
  stroke-linecap: round;
  stroke-linejoin: round;
  stroke-width: 2.4;
}

.refresh-button {
  color: #fff;
  background: linear-gradient(135deg, #18a7b6, #6672ef);
}

.refresh-button:disabled {
  cursor: not-allowed;
  opacity: 0.58;
}

.filter-bar {
  display: grid;
  grid-template-columns: auto 1fr 1fr;
  gap: 14px;
  align-items: end;
  border-radius: 28px;
  padding: 16px;
}

.range-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.range-tabs button,
.monitor-tabs button {
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.42);
  color: rgba(31, 35, 41, 0.62);
  font-weight: 850;
  transition:
    background 180ms ease,
    color 180ms ease,
    transform 180ms ease;
}

.range-tabs button {
  height: 42px;
  padding: 0 14px;
}

.range-tabs button:hover,
.range-tabs button.active,
.monitor-tabs button:hover,
.monitor-tabs button.active {
  color: #334f52;
  background: rgba(255, 255, 255, 0.82);
  transform: translateY(-2px);
}

.filter-bar label {
  display: grid;
  gap: 7px;
  color: rgba(31, 35, 41, 0.52);
  font-size: 12px;
  font-weight: 850;
}

.filter-bar input {
  width: 100%;
  height: 42px;
  border: 0;
  border-radius: 16px;
  outline: 0;
  padding: 0 12px;
  background: rgba(255, 255, 255, 0.62);
}

.monitor-tabs {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  border-radius: 30px;
  padding: 10px;
}

.monitor-tabs button {
  display: grid;
  gap: 4px;
  min-height: 72px;
  align-content: center;
  justify-items: start;
  padding: 0 18px;
  text-align: left;
}

.monitor-tabs span,
.monitor-tabs small {
  display: block;
}

.monitor-tabs small {
  color: rgba(31, 35, 41, 0.46);
  font-size: 12px;
}

.monitor-state {
  display: grid;
  gap: 4px;
  border-radius: 24px;
  padding: 18px;
  color: rgba(31, 35, 41, 0.62);
}

.monitor-state--error {
  color: #b24a3b;
}

@media (max-width: 860px) {
  .api-monitor-shell {
    width: min(520px, calc(100vw - 36px));
    margin-top: 18px;
  }

  .api-monitor-hero,
  .filter-bar,
  .monitor-tabs {
    grid-template-columns: 1fr;
  }

  .api-monitor-hero {
    justify-items: start;
  }
}
</style>
