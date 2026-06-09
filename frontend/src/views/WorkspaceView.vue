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
      <div class="chrome-actions">
        <UserBadge :user="authStore.currentUser" @logout="handleLogout" />
        <button class="settings-button" type="button" @click="settingsOpen = true">
          设置 <kbd>⌘</kbd><kbd>,</kbd>
        </button>
      </div>
    </header>

    <section class="board">
      <article class="widget profile">
        <div class="avatar">{{ userInitials }}</div>
        <h1>{{ greeting }}</h1>
        <p>{{ profileCopy }}</p>
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

    <SettingsDialog :open="settingsOpen" @close="settingsOpen = false" @logout="handleLogout" />
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import UserBadge from '../components/auth/UserBadge.vue'
import CommandWidget from '../components/workspace/CommandWidget.vue'
import SettingsDialog from '../components/settings/SettingsDialog.vue'
import { useAuthStore } from '../stores/authStore'
import { useHealthStore } from '../stores/healthStore'

const settingsOpen = ref(false)
const router = useRouter()
const authStore = useAuthStore()
const healthStore = useHealthStore()

const displayName = computed(() => authStore.currentUser?.displayName || authStore.currentUser?.username || 'dtools')
const userInitials = computed(() => displayName.value.slice(0, 2).toLowerCase())
const greeting = computed(() => `Good Morning, ${displayName.value}`)
const profileCopy = computed(() => {
  const dataScope = authStore.currentUser?.dataScope || '-'
  return `当前数据范围：${dataScope}。前端展示会跟随后端返回的权限摘要。`
})

function handleKeydown(event: KeyboardEvent) {
  if (event.metaKey && event.key === ',') {
    event.preventDefault()
    settingsOpen.value = true
  }

  if (event.key === 'Escape') {
    settingsOpen.value = false
  }
}

async function handleLogout() {
  await authStore.logout()
  settingsOpen.value = false
  await router.replace('/login')
}

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>
