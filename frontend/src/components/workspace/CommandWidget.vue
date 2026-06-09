<template>
  <section class="command-widget">
    <p class="eyebrow">Command line for daily tools</p>
    <div class="command-row">
      <input aria-label="输入工具命令" placeholder="输入、粘贴，或描述你要处理什么" />
      <button type="button" :disabled="!canExecute" :title="runTitle">Run</button>
    </div>
    <p v-if="!canExecute" class="permission-hint">
      当前账号缺少 tool:execute，Run 仅在前端禁用；后端接口仍是最终安全边界。
    </p>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '../../stores/authStore'
import { TOOL_EXECUTE_PERMISSION } from '../../utils/permission'

const authStore = useAuthStore()
const canExecute = computed(() => authStore.hasPermission(TOOL_EXECUTE_PERMISSION))
const runTitle = computed(() => (canExecute.value ? '执行工具命令' : '缺少 tool:execute 权限'))
</script>
