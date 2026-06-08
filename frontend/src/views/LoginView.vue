<template>
  <main class="auth-page">
    <section class="login-panel" aria-labelledby="login-title">
      <div class="auth-brand">
        <span class="traffic" aria-hidden="true">
          <i class="red"></i>
          <i class="yellow"></i>
          <i class="green"></i>
        </span>
        <strong>dtools</strong>
      </div>

      <div>
        <p class="eyebrow">Account access</p>
        <h1 id="login-title">登录个人工具空间</h1>
        <p>使用后端账号进入工作台，权限由 `/api/auth/me` 返回的权限码驱动。</p>
      </div>

      <form class="login-form" @submit.prevent="handleSubmit">
        <label>
          <span>用户名</span>
          <input v-model.trim="form.username" autocomplete="username" required />
        </label>
        <label>
          <span>密码</span>
          <input v-model="form.password" autocomplete="current-password" required type="password" />
        </label>
        <p v-if="authStore.loginError" class="form-error">{{ authStore.loginError }}</p>
        <button type="submit" :disabled="authStore.loading">
          {{ authStore.loading ? '登录中...' : '登录' }}
        </button>
      </form>
    </section>
  </main>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/authStore'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
  username: '',
  password: '',
})

async function handleSubmit() {
  const ok = await authStore.login({
    username: form.username,
    password: form.password,
  })

  if (ok) {
    const redirect = Array.isArray(route.query.redirect) ? route.query.redirect[0] : route.query.redirect
    await router.replace(redirect && redirect.startsWith('/') ? redirect : '/')
  }
}
</script>
