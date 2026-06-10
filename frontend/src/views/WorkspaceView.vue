<template>
  <main class="workspace">
    <div class="home-background" aria-hidden="true">
      <span class="bubble bubble-cyan"></span>
      <span class="bubble bubble-gold"></span>
      <span class="bubble bubble-mint"></span>
    </div>

    <section class="home-stage" aria-label="dtools 首页工作台">
      <aside class="home-card nav-card animate-card" style="--order: 1; --hover-x: 0px; --hover-y: -4px">
        <button class="nav-brand" type="button" aria-label="回到首页">
          <span class="avatar">{{ userInitials }}</span>
          <span>
            <strong>dtools</strong>
            <small>开发中</small>
          </span>
        </button>

        <p class="nav-section">General</p>
        <nav class="home-nav" aria-label="首页导航">
          <button
            v-for="item in navItems"
            :key="item.label"
            :class="{ active: item.active }"
            type="button"
            @click="handleNavClick(item)"
          >
            <span class="nav-icon" aria-hidden="true">
              <svg v-if="item.icon === 'menu'" viewBox="0 0 24 24">
                <path d="M4 7h16M4 12h16M4 17h16" />
              </svg>
              <svg v-else-if="item.icon === 'monitor'" viewBox="0 0 24 24">
                <path d="M4 18V8m5 10V5m5 13v-7m5 7V9" />
              </svg>
              <svg v-else-if="item.icon === 'history'" viewBox="0 0 24 24">
                <path d="M4 12a8 8 0 1 0 2.34-5.66M4 4v6h6" />
                <path d="M12 8v5l3 2" />
              </svg>
              <svg v-else-if="item.icon === 'protocol'" viewBox="0 0 24 24">
                <path d="m8 8-4 4 4 4m8-8 4 4-4 4M14 4l-4 16" />
              </svg>
              <svg v-else viewBox="0 0 24 24">
                <path d="M12 15.5A3.5 3.5 0 1 0 12 8.5a3.5 3.5 0 0 0 0 7Z" />
                <path d="M19 12h2M3 12h2m12.36 5.36 1.42 1.42M5.22 5.22l1.42 1.42m10.72 0 1.42-1.42M5.22 18.78l1.42-1.42M12 3v2m0 14v2" />
              </svg>
            </span>
            <span>{{ item.label }}</span>
          </button>
        </nav>
      </aside>

      <article class="home-card hi-card animate-card" style="--order: 2; --hover-x: 0px; --hover-y: -4px">
        <div class="hi-avatar">{{ userInitials }}</div>
        <h1>
          {{ greeting }}<br />
          I'm <span>{{ displayName }}</span>, Nice to<br />
          meet you!
        </h1>
        <p>{{ profileCopy }}</p>
      </article>

      <CommandWidget class="command-card animate-card" style="--order: 6; --hover-x: 0px; --hover-y: -4px" />

      <article class="home-card clock-card animate-card" style="--order: 3; --hover-x: 0px; --hover-y: -4px">
        <div class="segment-clock" :aria-label="`当前时间 ${currentTimeWithSeconds}`">
          <span>{{ currentTime }}</span>
          <span class="clock-second-wrap" aria-hidden="true">
            <span class="clock-second-colon">:</span>
            <span class="clock-seconds">{{ currentSeconds }}</span>
          </span>
        </div>
      </article>

      <article class="home-card calendar-card animate-card" style="--order: 4; --hover-x: 0px; --hover-y: -4px">
        <h2>{{ todayLabel }}</h2>
        <ol class="calendar-grid" aria-label="本月日历">
          <li v-for="weekday in weekdays" :key="weekday" class="weekday">{{ weekday }}</li>
          <li v-for="blank in calendarBlanks" :key="`blank-${blank}`"></li>
          <li v-for="day in calendarDays" :key="day" :class="{ today: day === todayDate }">{{ day }}</li>
        </ol>
      </article>

      <article class="home-card health-card animate-card" style="--order: 7; --hover-x: 0px; --hover-y: -4px">
        <h2>后端状态</h2>
        <p v-if="healthStore.loading">检查中...</p>
        <p v-else-if="healthStore.health">{{ healthStore.health.status }} · {{ healthStore.health.message }}</p>
        <p v-else-if="healthStore.error">连接失败：{{ healthStore.error }}</p>
        <p v-else>等待检查</p>
        <button type="button" @click="healthStore.loadHealth()">检查 API</button>
      </article>

      <div class="social-buttons animate-card" style="--order: 5; --hover-x: 0px; --hover-y: -4px" aria-label="快捷操作">
        <button type="button">Docs</button>
        <button type="button">API</button>
        <button type="button" @click="settingsOpen = true">Settings</button>
      </div>

      <article class="home-card mini-card history-card animate-card" style="--order: 8; --hover-x: 0px; --hover-y: -4px">
        <h2>最近执行</h2>
        <p>等待第一个工具运行记录</p>
      </article>

      <article class="home-card mini-card protocol-card animate-card" style="--order: 9; --hover-x: 0px; --hover-y: -4px">
        <h2>工具协议</h2>
        <p>ToolDefinition first</p>
      </article>
    </section>

    <header class="floating-chrome animate-card" style="--order: 10">
      <span class="traffic" aria-hidden="true">
        <i class="red"></i>
        <i class="yellow"></i>
        <i class="green"></i>
      </span>
      <UserBadge :user="authStore.currentUser" @logout="handleLogout" />
      <button class="settings-button" type="button" @click="settingsOpen = true">
        设置 <kbd>⌘</kbd><kbd>,</kbd>
      </button>
    </header>

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
const now = ref(new Date())

const displayName = computed(() => authStore.currentUser?.displayName || authStore.currentUser?.username || 'dtools')
const userInitials = computed(() => displayName.value.slice(0, 2).toLowerCase())
const greeting = computed(() => {
  const hour = now.value.getHours()
  if (hour >= 6 && hour < 12) {
    return 'Good Morning'
  }
  if (hour >= 12 && hour < 18) {
    return 'Good Afternoon'
  }
  if (hour >= 18 && hour < 22) {
    return 'Good Evening'
  }
  return 'Good Night'
})
const profileCopy = computed(() => {
  const dataScope = authStore.currentUser?.dataScope || '-'
  return `当前数据范围：${dataScope}。这里先保留首页壳和基础状态，后续再扩展具体工具。`
})
interface NavItem {
  label: string
  icon: 'menu' | 'monitor' | 'history' | 'protocol' | 'settings'
  active: boolean
  route?: string
}

const navItems: NavItem[] = [
  {
    label: '工具大厅',
    icon: 'menu',
    active: true,
  },
  {
    label: '接口监控',
    icon: 'monitor',
    active: false,
    route: '/api-monitor',
  },
  {
    label: '执行历史',
    icon: 'history',
    active: false,
  },
  {
    label: '协议中心',
    icon: 'protocol',
    active: false,
  },
  {
    label: '系统设置',
    icon: 'settings',
    active: false,
  },
]
const weekdays = ['一', '二', '三', '四', '五', '六', '日']
const currentTime = computed(() => {
  const hours = now.value.getHours().toString().padStart(2, '0')
  const minutes = now.value.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
})
const currentSeconds = computed(() => now.value.getSeconds().toString().padStart(2, '0'))
const currentTimeWithSeconds = computed(() => `${currentTime.value}:${currentSeconds.value}`)
const todayDate = computed(() => now.value.getDate())
const todayLabel = computed(() => {
  const year = now.value.getFullYear()
  const month = now.value.getMonth() + 1
  const date = now.value.getDate()
  const weekday = weekdays[(now.value.getDay() + 6) % 7]
  return `${year}/${month}/${date} 周${weekday}`
})
const calendarDays = computed(() => new Date(now.value.getFullYear(), now.value.getMonth() + 1, 0).getDate())
const calendarBlanks = computed(() => (new Date(now.value.getFullYear(), now.value.getMonth(), 1).getDay() + 6) % 7)

let clockTimer: number | undefined

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

function handleNavClick(item: NavItem) {
  if (item.route) {
    void router.push(item.route)
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
  clockTimer = window.setInterval(() => {
    now.value = new Date()
  }, 1000)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
  if (clockTimer) {
    window.clearInterval(clockTimer)
  }
})
</script>
