<template>
  <section class="errors-tab">
    <article class="monitor-panel">
      <div class="panel-heading">
        <div>
          <h2>异常类型排行</h2>
          <p>按异常类型聚合，快速定位高频失败原因。</p>
        </div>
      </div>
      <DistributionBars :items="summary?.errorTypeItems ?? []" empty-text="暂无异常类型数据" />
    </article>

    <article class="monitor-panel">
      <div class="panel-heading">
        <div>
          <h2>状态码分布</h2>
          <p>异常请求中的 HTTP 状态分布。</p>
        </div>
      </div>
      <DistributionBars :items="summary?.statusItems ?? []" empty-text="暂无状态码数据" />
    </article>

    <article class="monitor-panel monitor-panel--wide">
      <div class="panel-heading">
        <div>
          <h2>最近异常请求</h2>
          <p>保留 TraceID，方便回到日志链路继续追查。</p>
        </div>
      </div>
      <div v-if="recentErrors.length > 0" class="error-list">
        <article v-for="item in recentErrors" :key="item.traceId" class="error-row">
          <div>
            <span class="method-pill" :class="`method-pill--${methodTone(item.method)}`">{{ item.method }}</span>
            <strong>{{ item.uri }}</strong>
            <small>{{ item.exceptionType || '未知异常' }} · {{ item.exceptionMessage || '无异常摘要' }}</small>
          </div>
          <div>
            <code>{{ item.traceId }}</code>
            <small>{{ item.createdAt }} · {{ item.httpStatus || '-' }} · {{ formatDuration(item.costMs) }}</small>
          </div>
        </article>
      </div>
      <p v-else class="empty-copy">当前时间范围内没有异常请求。</p>
    </article>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ErrorSummary } from '../../types/apiMonitor'
import { formatDuration, methodTone } from '../../utils/apiMonitorFormat'
import DistributionBars from './DistributionBars.vue'

const props = defineProps<{
  summary: ErrorSummary | null
}>()

const recentErrors = computed(() => props.summary?.recentErrors ?? [])
</script>

<style scoped>
.errors-tab {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
}

.monitor-panel--wide {
  grid-column: 1 / -1;
}

.error-row {
  position: relative;
  overflow: hidden;
  border-radius: 20px;
  padding: 14px;
  background: rgba(255, 255, 255, 0.48);
}

.error-list {
  display: grid;
  gap: 12px;
}

.error-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 18px;
  align-items: center;
}

.error-row strong,
.error-row small {
  display: block;
}

.error-row strong {
  margin-top: 8px;
  color: #334f52;
  overflow-wrap: anywhere;
}

.error-row small {
  margin-top: 6px;
  color: rgba(31, 35, 41, 0.52);
}

code {
  display: block;
  max-width: 260px;
  overflow: hidden;
  border-radius: 999px;
  padding: 7px 10px;
  background: rgba(31, 35, 41, 0.07);
  color: #334f52;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.method-pill {
  display: inline-grid;
  place-items: center;
  border-radius: 999px;
  padding: 5px 9px;
  color: #fff;
  background: #7b888e;
  font-size: 11px;
  font-weight: 900;
}

.method-pill--get { background: #18a7b6; }
.method-pill--post { background: #6672ef; }
.method-pill--write { background: #e0a930; }
.method-pill--delete { background: #d66a5d; }

.empty-copy {
  color: rgba(31, 35, 41, 0.5);
}

@media (max-width: 860px) {
  .errors-tab,
  .error-row {
    grid-template-columns: 1fr;
  }
}
</style>
