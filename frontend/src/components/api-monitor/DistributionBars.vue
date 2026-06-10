<template>
  <ul v-if="rows.length > 0" class="distribution-bars">
    <li v-for="item in rows" :key="item.label">
      <span>{{ item.label }}</span>
      <strong>{{ formatNumber(item.count) }}</strong>
      <i :style="{ width: `${item.percent}%` }"></i>
    </li>
  </ul>
  <p v-else class="empty-copy">{{ emptyText }}</p>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ErrorTypeItem, LatencyBucket, StatusItem } from '../../types/apiMonitor'
import { distributionLabel, distributionTotal, formatNumber } from '../../utils/apiMonitorFormat'

const props = defineProps<{
  items: Array<StatusItem | ErrorTypeItem | LatencyBucket>
  emptyText: string
}>()

const rows = computed(() => {
  const total = distributionTotal(props.items)
  return props.items.map((item) => ({
    ...item,
    label: distributionLabel(item),
    percent: total > 0 ? Math.max((item.count / total) * 100, 6) : 0,
  }))
})
</script>

<style scoped>
.distribution-bars {
  display: grid;
  gap: 12px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.distribution-bars li {
  position: relative;
  overflow: hidden;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
  border-radius: 20px;
  padding: 14px;
  background: rgba(255, 255, 255, 0.48);
}

.distribution-bars i {
  position: absolute;
  inset: auto auto 0 0;
  height: 4px;
  border-radius: 999px;
  background: linear-gradient(90deg, #d66a5d, #e0a930);
}

.empty-copy {
  color: rgba(31, 35, 41, 0.5);
}
</style>
