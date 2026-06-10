<template>
  <article class="metric-tile">
    <span class="metric-tile__label">{{ label }}</span>
    <strong>{{ value }}</strong>
    <small v-if="hint">{{ hint }}</small>
  </article>
</template>

<script setup lang="ts">
defineProps<{
  label: string
  value: string
  hint?: string
}>()
</script>

<style scoped>
.metric-tile {
  min-height: 118px;
  display: grid;
  align-content: space-between;
  gap: 14px;
  border: 1px solid rgba(255, 255, 255, 0.76);
  border-radius: 28px;
  padding: 18px;
  background: rgba(255, 255, 255, 0.52);
  box-shadow: inset 0 0 18px rgba(255, 255, 255, 0.2);
}

.metric-tile__label,
.metric-tile small {
  color: rgba(31, 35, 41, 0.56);
  font-size: 12px;
  font-weight: 800;
}

.metric-tile strong {
  color: #334f52;
  font-size: clamp(24px, 3vw, 34px);
  line-height: 1;
  letter-spacing: 0;
}
</style>
