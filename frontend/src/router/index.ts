import { createRouter, createWebHistory } from 'vue-router'
import type { RouteLocationNormalized } from 'vue-router'
import { useAuthStore } from '../stores/authStore'
import ApiMonitorView from '../views/ApiMonitorView.vue'
import ForbiddenView from '../views/ForbiddenView.vue'
import LoginView from '../views/LoginView.vue'
import WorkspaceView from '../views/WorkspaceView.vue'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: {
        public: true,
      },
    },
    {
      path: '/403',
      name: 'forbidden',
      component: ForbiddenView,
      meta: {
        public: true,
      },
    },
    {
      path: '/',
      name: 'workspace',
      component: WorkspaceView,
      meta: {
        requiresAuth: true,
      },
    },
    {
      path: '/api-monitor',
      name: 'apiMonitor',
      component: ApiMonitorView,
      meta: {
        requiresAuth: true,
      },
    },
  ],
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()

  if (to.meta.public) {
    if (to.name === 'login' && (await authStore.ensureCurrentUser())) {
      return redirectTarget(to)
    }

    return true
  }

  if (!to.meta.requiresAuth) {
    return true
  }

  const authenticated = await authStore.ensureCurrentUser()
  if (!authenticated) {
    return {
      path: '/login',
      query: {
        redirect: to.fullPath,
      },
    }
  }

  const permissions = Array.isArray(to.meta.permissions) ? to.meta.permissions : []
  if (permissions.length > 0 && !authStore.hasAllPermissions(permissions)) {
    return {
      path: '/403',
      query: {
        from: to.fullPath,
      },
    }
  }

  return true
})

function redirectTarget(to: RouteLocationNormalized): string {
  const redirect = Array.isArray(to.query.redirect) ? to.query.redirect[0] : to.query.redirect
  return redirect && redirect.startsWith('/') ? redirect : '/'
}

declare module 'vue-router' {
  interface RouteMeta {
    public?: boolean
    requiresAuth?: boolean
    permissions?: string[]
  }
}
