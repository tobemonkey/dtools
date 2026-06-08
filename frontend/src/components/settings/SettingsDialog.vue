<template>
  <div v-if="open" class="settings-backdrop" role="dialog" aria-modal="true" aria-labelledby="settings-title">
    <section class="settings-dialog">
      <header>
        <div>
          <h2 id="settings-title">偏好设置</h2>
          <p>可通过 <kbd>⌘</kbd> <kbd>,</kbd> 呼出。</p>
        </div>
        <button type="button" aria-label="关闭设置" @click="$emit('close')">×</button>
      </header>
      <div class="settings-grid">
        <div class="setting-card">
          <strong>启动入口</strong>
          <span>打开后进入个人工具空间。</span>
        </div>
        <div class="setting-card">
          <strong>后端服务</strong>
          <span>通过 REST API 调用独立 Spring Boot 服务。</span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  open: boolean
}>()

defineEmits<{
  close: []
}>()
</script>
