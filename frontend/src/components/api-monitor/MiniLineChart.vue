<template>
  <div class="mini-line-chart">
    <svg viewBox="0 0 320 120" role="img" :aria-label="title">
      <defs>
        <linearGradient :id="gradientId" x1="0" x2="1" y1="0" y2="0">
          <stop offset="0%" stop-color="#35bfab" />
          <stop offset="100%" stop-color="#6672ef" />
        </linearGradient>
      </defs>
      <polyline
        v-if="path"
        fill="none"
        :stroke="`url(#${gradientId})`"
        stroke-linecap="round"
        stroke-linejoin="round"
        stroke-width="7"
        :points="path"
      />
      <text v-else x="160" y="66" text-anchor="middle">暂无趋势数据</text>
    </svg>
    <div class="mini-line-chart__labels" v-if="firstLabel || lastLabel">
      <span>{{ firstLabel }}</span>
      <span>{{ lastLabel }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { TrendPoint } from '../../types/apiMonitor'
import { trendToPolyline } from '../../utils/apiMonitorFormat'

const props = withDefaults(
  defineProps<{
    title: string
    points: TrendPoint[]
    valueKey?: keyof TrendPoint
  }>(),
  {
    valueKey: 'totalCount',
  },
)

const gradientId = `chart-${Math.random().toString(36).slice(2)}`
const path = computed(() => trendToPolyline(props.points, 320, 110, props.valueKey))
const firstLabel = computed(() => props.points[0]?.bucketTime ?? '')
const lastLabel = computed(() => props.points[props.points.length - 1]?.bucketTime ?? '')
</script>

<style scoped>
.mini-line-chart {
  display: grid;
  gap: 10px;
}

svg {
  width: 100%;
  min-height: 150px;
  border-radius: 28px;
  padding: 18px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.58), rgba(255, 255, 255, 0.28)),
    repeating-linear-gradient(0deg, rgba(51, 79, 82, 0.06) 0 1px, transparent 1px 32px);
}

text {
  fill: rgba(31, 35, 41, 0.44);
  font-size: 14px;
  font-weight: 800;
}

.mini-line-chart__labels {
  display: flex;
  justify-content: space-between;
  color: rgba(31, 35, 41, 0.48);
  font-size: 12px;
  font-weight: 750;
}
</style>
