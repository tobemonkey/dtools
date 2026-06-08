<template>
  <main class="workspace">
    <header class="chrome">
      <div class="brand">
        <span class="traffic" aria-hidden="true">
          <i class="red"></i>
          <i class="yellow"></i>
          <i class="green"></i>
        </span>
        <strong>dtools</strong>
        <span>personal tool space</span>
      </div>
      <button class="settings-button" type="button" @click="settingsOpen = true">
        设置 <kbd>⌘</kbd><kbd>,</kbd>
      </button>
    </header>

    <section class="board">
      <article class="widget profile">
        <div class="avatar">dt</div>
        <h1>Good Morning</h1>
        <p>工具不多时，入口应该像个人空间，而不是后台菜单。</p>
      </article>

      <article class="widget hero-card">
        <div class="orb" aria-hidden="true"></div>
        <h2>把小工具做成<br /><span>每天想打开的地方</span></h2>
        <p>当前框架只验证前后端分离链路，不包含具体工具功能。</p>
      </article>

      <CommandWidget />

      <article class="widget health-card">
        <h2>后端状态</h2>
        <p v-if="healthStore.loading">检查中...</p>
        <p v-else-if="healthStore.health">{{ healthStore.health.status }} · {{ healthStore.health.message }}</p>
        <p v-else-if="healthStore.error">连接失败：{{ healthStore.error }}</p>
        <p v-else>等待检查</p>
        <button type="button" @click="healthStore.loadHealth()">检查 API</button>
      </article>
    </section>

    <SettingsDialog :open="settingsOpen" @close="settingsOpen = false" />
  </main>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import CommandWidget from '../components/workspace/CommandWidget.vue'
import SettingsDialog from '../components/settings/SettingsDialog.vue'
import { useHealthStore } from '../stores/healthStore'

const settingsOpen = ref(false)
const healthStore = useHealthStore()

function handleKeydown(event: KeyboardEvent) {
  if (event.metaKey && event.key === ',') {
    event.preventDefault()
    settingsOpen.value = true
  }

  if (event.key === 'Escape') {
    settingsOpen.value = false
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>
