<template>
  <section class="monitor-panel">
    <div class="panel-heading">
      <div>
        <h2>慢接口排行</h2>
        <p>按长尾耗时观察接口，重点看 t99 与 timeoutRate。</p>
      </div>
    </div>

    <div v-if="endpoints.length > 0" class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>接口</th>
            <th>Count</th>
            <th>Avg</th>
            <th>T90</th>
            <th>T95</th>
            <th class="hot">T99</th>
            <th>Max</th>
            <th>Timeout</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in endpoints" :key="`${item.method}-${item.uri}`">
            <td>
              <span class="method-pill" :class="`method-pill--${methodTone(item.method)}`">{{ item.method }}</span>
              <strong>{{ item.uri }}</strong>
            </td>
            <td>{{ formatNumber(item.requestCount) }}</td>
            <td>{{ formatDuration(item.avgCostMs) }}</td>
            <td>{{ formatDuration(item.t90CostMs) }}</td>
            <td>{{ formatDuration(item.t95CostMs) }}</td>
            <td class="hot">{{ formatDuration(item.t99CostMs) }}</td>
            <td>{{ formatDuration(item.maxCostMs) }}</td>
            <td>{{ formatRate(item.timeoutRate) }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <p v-else class="empty-copy">当前时间范围内暂无慢接口记录。</p>
  </section>
</template>

<script setup lang="ts">
import type { SlowEndpoint } from '../../types/apiMonitor'
import { formatDuration, formatNumber, formatRate, methodTone } from '../../utils/apiMonitorFormat'

defineProps<{
  endpoints: SlowEndpoint[]
}>()
</script>

<style scoped>
.table-wrap {
  overflow-x: auto;
}

table {
  width: 100%;
  min-width: 860px;
  border-spacing: 0 10px;
}

th {
  padding: 0 14px 4px;
  color: rgba(31, 35, 41, 0.48);
  font-size: 12px;
  text-align: left;
  text-transform: uppercase;
}

td {
  padding: 14px;
  background: rgba(255, 255, 255, 0.48);
  color: rgba(31, 35, 41, 0.7);
  font-size: 13px;
  font-weight: 750;
  white-space: nowrap;
}

td:first-child {
  border-radius: 18px 0 0 18px;
  white-space: normal;
}

td:last-child {
  border-radius: 0 18px 18px 0;
}

td:first-child strong {
  display: block;
  margin-top: 8px;
  color: #334f52;
  overflow-wrap: anywhere;
}

.hot {
  color: #b24a3b;
  font-weight: 950;
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
</style>
