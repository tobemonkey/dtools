<template>
  <section class="overview-tab">
    <div class="metric-grid">
      <MetricTile label="请求数" :value="formatNumber(overview?.totalCount)" hint="当前范围内总调用" />
      <MetricTile label="平均耗时" :value="formatDuration(overview?.avgCostMs)" hint="接口整体响应" />
      <MetricTile label="P95" :value="formatDuration(overview?.t95CostMs)" hint="95% 请求耗时以内" />
      <MetricTile label="P99" :value="formatDuration(overview?.t99CostMs)" hint="长尾响应观察" />
      <MetricTile label="异常率" :value="formatRate(overview?.errorRate)" :hint="`${formatNumber(overview?.errorCount)} 次异常`" />
    </div>

    <div class="overview-layout">
      <article class="monitor-panel monitor-panel--wide">
        <div class="panel-heading">
          <div>
            <h2>调用趋势</h2>
            <p>按时间桶展示请求量，辅助判断接口负载波动。</p>
          </div>
        </div>
        <MiniLineChart title="接口调用趋势" :points="overview?.trendPoints ?? []" />
      </article>

      <article class="monitor-panel">
        <div class="panel-heading">
          <div>
            <h2>状态分布</h2>
            <p>HTTP 状态码聚合。</p>
          </div>
        </div>
        <ul v-if="statusItems.length > 0" class="distribution-list">
          <li v-for="item in statusItems" :key="item.status">
            <span>{{ item.status }}</span>
            <strong>{{ formatNumber(item.count) }}</strong>
            <i :style="{ width: `${item.percent}%` }"></i>
          </li>
        </ul>
        <p v-else class="empty-copy">暂无状态分布</p>
      </article>

      <article class="monitor-panel">
        <div class="panel-heading">
          <div>
            <h2>最慢接口</h2>
            <p>当前窗口内耗时最高的接口。</p>
          </div>
        </div>
        <div v-if="overview?.slowestUri" class="slowest-box">
          <span class="method-pill">{{ overview.slowestMethod || '-' }}</span>
          <strong>{{ overview.slowestUri }}</strong>
          <small>{{ formatDuration(overview.slowestCostMs) }}</small>
        </div>
        <p v-else class="empty-copy">暂无最慢接口数据</p>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ApiOverview } from '../../types/apiMonitor'
import { distributionTotal, formatDuration, formatNumber, formatRate } from '../../utils/apiMonitorFormat'
import MetricTile from './MetricTile.vue'
import MiniLineChart from './MiniLineChart.vue'

const props = defineProps<{
  overview: ApiOverview | null
}>()

const statusItems = computed(() => {
  const items = props.overview?.statusSegments ?? []
  const total = distributionTotal(items)
  return items.map((item) => ({
    ...item,
    percent: total > 0 ? Math.max((item.count / total) * 100, 6) : 0,
  }))
})
</script>

<style scoped>
.overview-tab,
.overview-layout {
  display: grid;
  gap: 18px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 16px;
}

.overview-layout {
  grid-template-columns: minmax(0, 1.4fr) minmax(260px, 0.8fr);
}

.monitor-panel--wide {
  grid-row: span 2;
}

.distribution-list {
  display: grid;
  gap: 12px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.distribution-list li {
  position: relative;
  overflow: hidden;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
  border-radius: 18px;
  padding: 14px;
  background: rgba(255, 255, 255, 0.48);
}

.distribution-list i {
  position: absolute;
  inset: auto auto 0 0;
  height: 4px;
  border-radius: 999px;
  background: linear-gradient(90deg, #35bfab, #6672ef);
}

.slowest-box {
  display: grid;
  gap: 12px;
}

.slowest-box strong {
  overflow-wrap: anywhere;
}

.slowest-box small {
  color: #18a7b6;
  font-size: 28px;
  font-weight: 900;
}

.method-pill {
  width: fit-content;
  border-radius: 999px;
  padding: 6px 10px;
  color: #fff;
  background: linear-gradient(135deg, #35bfab, #6672ef);
  font-size: 12px;
  font-weight: 900;
}

.empty-copy {
  color: rgba(31, 35, 41, 0.5);
}

@media (max-width: 980px) {
  .metric-grid,
  .overview-layout {
    grid-template-columns: 1fr 1fr;
  }

  .monitor-panel--wide {
    grid-column: 1 / -1;
    grid-row: auto;
  }
}

@media (max-width: 640px) {
  .metric-grid,
  .overview-layout {
    grid-template-columns: 1fr;
  }
}
</style>
