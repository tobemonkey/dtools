<template>
  <main class="auth-page">
    <section class="login-panel forbidden-panel" aria-labelledby="forbidden-title">
      <div class="auth-brand">
        <span class="traffic" aria-hidden="true">
          <i class="red"></i>
          <i class="yellow"></i>
          <i class="green"></i>
        </span>
        <strong>dtools</strong>
      </div>

      <div>
        <p class="eyebrow">403 Forbidden</p>
        <h1 id="forbidden-title">当前账号权限不足</h1>
        <p>前端只做体验提示，最终访问控制仍由后端接口和权限码校验决定。</p>
      </div>

      <div class="forbidden-actions">
        <RouterLink to="/">返回工作台</RouterLink>
        <button type="button" @click="handleLogout">退出登录</button>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/authStore'

const router = useRouter()
const authStore = useAuthStore()

async function handleLogout() {
  await authStore.logout()
  await router.replace('/login')
}
</script>
