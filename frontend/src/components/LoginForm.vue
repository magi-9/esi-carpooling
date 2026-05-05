<template>
  <n-card title="Welcome Back" style="max-width: 400px; margin: 0 auto">
    <n-form @submit.prevent="handleLogin" :show-label="false">
      <n-form-item label="Email">
        <n-input
          v-model:value="email"
          type="email"
          placeholder="your@email.com"
          :disabled="loading"
          clearable
          autocomplete="username"
        />
      </n-form-item>

      <n-form-item label="Password">
        <n-input
          v-model:value="password"
          type="password"
          placeholder="Enter your password"
          :disabled="loading"
          show-password-toggle
          autocomplete="current-password"
        />
      </n-form-item>

      <n-space vertical size="large">
        <n-button type="primary" :loading="loading" block attr-type="submit">
          {{ loading ? 'Signing in...' : 'Sign In' }}
        </n-button>

        <n-alert v-if="errorMessage" type="error" :show-icon="false" style="text-align: center">
          {{ errorMessage }}
        </n-alert>

        <n-divider />

        <n-space justify="center">
          <n-text>Don't have an account?</n-text>
          <router-link to="/register" style="text-decoration: none">
            <n-button text type="primary">Register here</n-button>
          </router-link>
        </n-space>
      </n-space>
    </n-form>
  </n-card>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const router = useRouter();

// Form State
const email = ref('');
const password = ref('');

// UI State
const loading = ref(false);
const errorMessage = ref('');

const handleLogin = async () => {
  // Reset Error
  errorMessage.value = '';
  loading.value = true;

  try {
    // The store handles the API request, saving the token, and starting the refresh timer
    await authStore.login(email.value, password.value);

    // Redirect to the home page after login
    router.push('/');
  } catch (error) {
    // Handle Specific API Errors based on OpenAPI spec
    if (error.response && error.response.status === 401) {
      errorMessage.value = 'Invalid email or password.';
    } else {
      errorMessage.value = 'An unexpected error occurred. Please try again.';
    }
  } finally {
    loading.value = false;
  }
};
</script>
