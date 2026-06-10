<template>
  <section class="interfaces-tab">
    <article class="monitor-panel interfaces-list">
      <div class="panel-heading">
        <div>
          <h2>接口列表</h2>
          <p>按 method + uri 聚合，点击后查看细分趋势。</p>
        </div>
      </div>

      <form class="search-row" @submit.prevent="$emit('search')">
        <input
          :value="keyword"
          type="search"
          placeholder="搜索 URI 或方法"
          @input="$emit('update:keyword', ($event.target as HTMLInputElement).value)"
        />
        <button type="submit">搜索</button>
      </form>

      <div v-if="interfaces.length > 0" class="interface-list">
        <button
          v-for="item in interfaces"
          :key="`${item.method}-${item.uri}`"
          type="button"
          :class="{ active: isSelected(item) }"
          @click="$emit('select', item)"
        >
          <span class="method-pill" :class="`method-pill--${methodTone(item.method)}`">{{ item.method }}</span>
          <strong>{{ item.uri }}</strong>
          <small>
            {{ formatNumber(item.requestCount) }} 次 · 成功率 {{ formatRate(successRateFromErrorRate(item.errorRate)) }} · t99
            {{ formatDuration(item.t99CostMs) }}
          </small>
        </button>
      </div>
      <p v-else class="empty-copy">暂无接口聚合数据，可调整范围或关键词后刷新。</p>
    </article>

    <article class="monitor-panel detail-panel">
      <div class="panel-heading">
        <div>
          <h2>接口详情</h2>
          <p v-if="selectedInterface">{{ selectedInterface.method }} {{ selectedInterface.uri }}</p>
          <p v-else>选择左侧接口后查看耗时、状态与异常分布。</p>
        </div>
      </div>

      <div v-if="detailLoading" class="state-block">接口详情加载中...</div>
      <div v-else-if="detail" class="detail-content">
        <div class="metric-grid">
          <MetricTile label="调用数" :value="formatNumber(detail.summary.requestCount)" />
          <MetricTile label="成功率" :value="formatRate(successRateFromErrorRate(detail.summary.errorRate))" />
          <MetricTile label="T99" :value="formatDuration(detail.summary.t99CostMs)" />
          <MetricTile label="异常数" :value="formatNumber(detail.summary.errorCount)" />
        </div>

        <MiniLineChart title="接口耗时趋势" :points="detail.trendPoints" value-key="avgCostMs" />

        <div class="detail-grid">
          <section>
            <h3>状态码</h3>
            <DistributionBars :items="detail.statusItems" empty-text="暂无状态码分布" />
          </section>
          <section>
            <h3>异常类型</h3>
            <DistributionBars :items="detail.errorTypeItems" empty-text="暂无异常类型分布" />
          </section>
          <section>
            <h3>耗时桶</h3>
            <ul v-if="detail.latencyBuckets.length > 0" class="bucket-list">
              <li v-for="bucket in detail.latencyBuckets" :key="bucket.bucketName">
                <span>{{ bucket.bucketName }}</span>
                <strong>{{ formatNumber(bucket.count) }}</strong>
              </li>
            </ul>
            <p v-else class="empty-copy">暂无耗时桶数据</p>
          </section>
          <section>
            <h3>最近异常</h3>
            <ul v-if="detail.recentErrors.length > 0" class="recent-errors">
              <li v-for="item in detail.recentErrors" :key="item.traceId">
                <code>{{ item.traceId }}</code>
                <span>{{ item.exceptionType || '未知异常' }}</span>
                <small>{{ item.createdAt }} · {{ formatDuration(item.costMs) }}</small>
              </li>
            </ul>
            <p v-else class="empty-copy">暂无最近异常</p>
          </section>
        </div>
      </div>
      <p v-else class="empty-copy">还没有选中的接口。</p>
    </article>
  </section>
</template>

<script setup lang="ts">
import type { InterfaceDetail, InterfaceSummary } from '../../types/apiMonitor'
import {
  formatDuration,
  formatNumber,
  formatRate,
  methodTone,
  successRateFromErrorRate,
} from '../../utils/apiMonitorFormat'
import DistributionBars from './DistributionBars.vue'
import MetricTile from './MetricTile.vue'
import MiniLineChart from './MiniLineChart.vue'

const props = defineProps<{
  interfaces: InterfaceSummary[]
  selectedInterface: InterfaceSummary | null
  detail: InterfaceDetail | null
  keyword: string
  detailLoading: boolean
}>()

defineEmits<{
  search: []
  select: [item: InterfaceSummary]
  'update:keyword': [value: string]
}>()

function isSelected(item: InterfaceSummary): boolean {
  return props.selectedInterface?.method === item.method && props.selectedInterface.uri === item.uri
}
</script>

<style scoped>
.interfaces-tab {
  display: grid;
  grid-template-columns: minmax(300px, 0.72fr) minmax(0, 1.28fr);
  gap: 18px;
}

.search-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
}

.search-row input {
  min-width: 0;
  height: 46px;
  border: 0;
  border-radius: 18px;
  outline: 0;
  padding: 0 14px;
  background: rgba(255, 255, 255, 0.64);
}

.search-row button {
  height: 46px;
  border-radius: 18px;
  padding: 0 16px;
  color: #fff;
  background: linear-gradient(135deg, #18a7b6, #6672ef);
  font-weight: 850;
}

.interface-list {
  display: grid;
  gap: 10px;
  margin-top: 16px;
}

.interface-list button {
  display: grid;
  justify-items: start;
  gap: 8px;
  border-radius: 22px;
  padding: 14px;
  color: rgba(31, 35, 41, 0.62);
  background: rgba(255, 255, 255, 0.46);
  text-align: left;
  transition:
    background 180ms ease,
    transform 180ms ease;
}

.interface-list button:hover,
.interface-list button.active {
  background: rgba(255, 255, 255, 0.78);
  transform: translateY(-2px);
}

.interface-list strong {
  color: #334f52;
  overflow-wrap: anywhere;
}

.interface-list small {
  line-height: 1.5;
}

.detail-content {
  display: grid;
  gap: 18px;
}

.metric-grid,
.detail-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.detail-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.detail-grid section {
  border-radius: 26px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.36);
}

.detail-grid h3 {
  margin: 0 0 12px;
  color: #334f52;
  font-size: 15px;
}

.bucket-list,
.recent-errors {
  display: grid;
  gap: 10px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.bucket-list li,
.recent-errors li {
  border-radius: 16px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.5);
}

.bucket-list li {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.recent-errors code,
.recent-errors span,
.recent-errors small {
  display: block;
}

.recent-errors code {
  overflow: hidden;
  color: #334f52;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recent-errors span {
  margin-top: 8px;
  color: #b24a3b;
  font-weight: 850;
}

.recent-errors small {
  margin-top: 4px;
  color: rgba(31, 35, 41, 0.5);
}

.state-block,
.empty-copy {
  color: rgba(31, 35, 41, 0.5);
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

@media (max-width: 1080px) {
  .interfaces-tab,
  .detail-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .metric-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 520px) {
  .metric-grid,
  .search-row {
    grid-template-columns: 1fr;
  }
}
</style>
