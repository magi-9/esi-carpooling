<template>
  <n-layout-header style="background: #2c3e50; padding: 0 24px; height: 64px;">
    <n-space align="center" justify="space-between" style="height: 100%;">
      <n-space align="center">
        <router-link to="/" style="text-decoration: none;">
          <n-text style="color: white; font-weight: bold; font-size: 18px;">Carpooling</n-text>
        </router-link>

        <n-space v-if="isAuthenticated" align="center">
          <router-link to="/search" style="text-decoration: none;">
            <n-button text style="color: #ecf0f1;">Search Rides</n-button>
          </router-link>
          <router-link to="/payments/new" style="text-decoration: none;">
            <n-button text style="color: #ecf0f1;">New Payment</n-button>
          </router-link>
        </n-space>
      </n-space>

      <n-space align="center">
        <template v-if="!isAuthenticated">
          <router-link to="/login" style="text-decoration: none;">
            <n-button ghost style="color: #ecf0f1; border-color: rgba(255,255,255,0.35);">Login</n-button>
          </router-link>
          <router-link to="/register" style="text-decoration: none;">
            <n-button type="primary">Register</n-button>
          </router-link>
        </template>
        <template v-else>
          <n-button ghost style="color: #ecf0f1; border-color: rgba(255,255,255,0.35);" @click="handleLogout">Logout</n-button>
        </template>
      </n-space>
    </n-space>
  </n-layout-header>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'

const authStore = useAuthStore()
const router = useRouter()

const { isAuthenticated } = storeToRefs(authStore)

const handleLogout = async () => {
  try {
    await authStore.logout()
    router.push('/login')
  } catch (error) {
    console.error('Logout failed:', error)
    // Even if backend logout fails, the local logout should have cleared the token
    router.push('/login')
  }
}
</script>