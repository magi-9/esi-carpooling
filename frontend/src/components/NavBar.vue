<template>
  <nav style="background:#2c3e50;padding:12px 24px;display:flex;gap:20px;align-items:center">
    <RouterLink to="/" style="color:white;font-weight:bold;font-size:18px;text-decoration:none">Carpooling</RouterLink>

    <!-- Show navigation links only when authenticated -->
    <div v-if="authStore.isAuthenticated" style="display:flex;gap:20px">
      <RouterLink to="/search" style="color:#ecf0f1;text-decoration:none">Search Rides</RouterLink>
      <RouterLink to="/payments/new" style="color:#ecf0f1;text-decoration:none">New Payment</RouterLink>
    </div>

    <!-- Authentication buttons -->
    <div style="margin-left:auto;display:flex;gap:10px">
      <template v-if="!authStore.isAuthenticated">
        <RouterLink to="/login" style="color:#ecf0f1;text-decoration:none;padding:8px 16px;border:1px solid #ecf0f1;border-radius:4px">Login</RouterLink>
        <RouterLink to="/register" style="color:#ecf0f1;text-decoration:none;padding:8px 16px;background:#3498db;border-radius:4px">Register</RouterLink>
      </template>
      <template v-else>
        <button @click="handleLogout" style="color:#ecf0f1;background:none;border:1px solid #ecf0f1;padding:8px 16px;border-radius:4px;cursor:pointer">Logout</button>
      </template>
    </div>
  </nav>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

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