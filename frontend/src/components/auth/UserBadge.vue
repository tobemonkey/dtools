<template>
  <div class="user-badge">
    <div class="user-badge__avatar">{{ initials }}</div>
    <div>
      <strong>{{ displayName }}</strong>
      <span>{{ roleText }} · {{ dataScopeText }}</span>
    </div>
    <button type="button" @click="$emit('logout')">退出</button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { CurrentUserDTO } from '../../types/auth'

const props = defineProps<{
  user: CurrentUserDTO | null
}>()

defineEmits<{
  logout: []
}>()

const displayName = computed(() => props.user?.displayName || props.user?.username || '未登录')
const initials = computed(() => displayName.value.slice(0, 2).toLowerCase())
const roleText = computed(() => props.user?.roles.join(' / ') || 'no role')
const dataScopeText = computed(() => (props.user?.dataScope ? `scope:${props.user.dataScope}` : 'scope:-'))
</script>
