<template>
  <n-card title="Create an Account" style="max-width: 400px; margin: 0 auto">
    <n-form @submit.prevent="handleRegister" :show-label="false">
      <n-form-item label="Email">
        <n-input
          v-model:value="email"
          type="email"
          placeholder="your@email.com"
          :disabled="loading"
          clearable
          :input-props="{
            autocomplete: 'email'
          }"
        />
      </n-form-item>

      <n-form-item label="Password">
        <n-input
          v-model:value="password"
          type="password"
          placeholder="Min. 8 characters"
          :disabled="loading"
          show-password-toggle
          :minlength="8"
          :input-props="{
            autocomplete: 'new-password'
          }"
        />
      </n-form-item>

      <n-form-item label="I want to register as: (Select at least one)">
        <n-checkbox-group v-model:value="selectedRoles" :disabled="loading">
          <n-space>
            <n-checkbox value="PASSENGER" label="Passenger" />
            <n-checkbox value="DRIVER" label="Driver" />
          </n-space>
        </n-checkbox-group>
        <n-alert v-if="roleError" type="warning" :show-icon="false" style="margin-top: 8px">
          {{ roleError }}
        </n-alert>
      </n-form-item>

      <n-space vertical size="large">
        <n-button type="primary" :loading="loading" block attr-type="submit">
          {{ loading ? 'Creating account...' : 'Register' }}
        </n-button>

        <n-alert v-if="apiError" type="error" :show-icon="false">
          {{ apiError }}
        </n-alert>

        <n-divider />

        <n-space justify="center">
          <n-text>Already have an account?</n-text>
          <router-link to="/login" style="text-decoration: none">
            <n-button text type="primary">Login here</n-button>
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
const selectedRoles = ref(['PASSENGER']); // Pre-select PASSENGER to satisfy minItems: 1

// UI State
const loading = ref(false);
const roleError = ref('');
const apiError = ref('');

const handleRegister = async () => {
  // 1. Reset Errors
  roleError.value = '';
  apiError.value = '';

  // 2. Client-side Validation (Enforcing OpenAPI constraint: minItems 1)
  if (selectedRoles.value.length === 0) {
    roleError.value = 'You must select at least one role (Passenger or Driver).';
    return;
  }

  // 3. API Submission
  loading.value = true;
  try {
    await authStore.register(email.value, password.value, selectedRoles.value);

    // The store action automatically sets the JWT token on success.
    // Redirect the user to the home page.
    router.push('/');
  } catch (error) {
    // 4. Handle Specific API Errors based on OpenAPI spec
    if (error.response) {
      if (error.response.status === 400) {
        apiError.value = 'Invalid input or a user with this email already exists.';
      } else if (error.response.status === 503) {
        apiError.value = 'Registration is temporarily down. Please try again later.';
      } else {
        apiError.value = `Error: ${error.response.statusText}`;
      }
    } else {
      apiError.value = 'Network error. Please check your connection.';
    }
  } finally {
    loading.value = false;
  }
};
</script>
