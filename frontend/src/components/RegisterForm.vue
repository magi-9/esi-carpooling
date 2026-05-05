<template>
  <div class="register-container">
    <h2>Create an Account</h2>
    
    <form @submit.prevent="handleRegister">
      <!-- Email Input -->
      <div class="form-group">
        <label for="email">Email</label>
        <input 
          id="email" 
          v-model="email" 
          type="email" 
          placeholder="your@email.com"
          required 
        />
      </div>

      <!-- Password Input -->
      <div class="form-group">
        <label for="password">Password</label>
        <input 
          id="password" 
          v-model="password" 
          type="password" 
          minlength="8" 
          placeholder="Min. 8 characters"
          required 
        />
      </div>

      <!-- Roles Selection -->
      <div class="form-group">
        <label>I want to register as a: (Select at least one)</label>
        <div class="checkbox-group">
          <label>
            <input 
              type="checkbox" 
              value="PASSENGER" 
              v-model="selectedRoles" 
            />
            Passenger
          </label>
          <label>
            <input 
              type="checkbox" 
              value="DRIVER" 
              v-model="selectedRoles" 
            />
            Driver
          </label>
        </div>
        <!-- Client-side validation error for roles -->
        <p v-if="roleError" class="error-text">{{ roleError }}</p>
      </div>

      <!-- Submit Button -->
      <button type="submit" :disabled="loading">
        {{ loading ? 'Creating account...' : 'Register' }}
      </button>

      <!-- API Error Display -->
      <p v-if="apiError" class="error-text">{{ apiError }}</p>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth'; // Assuming the Vite alias is fixed

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
    // Redirect the user to your main application view.
    router.push('/search'); 
    
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

<style scoped>
.register-container {
  max-width: 400px;
  margin: 0 auto;
  padding: 2rem;
  border: 1px solid #ccc;
  border-radius: 8px;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: bold;
}

.form-group input[type="email"],
.form-group input[type="password"] {
  width: 100%;
  padding: 0.5rem;
  box-sizing: border-box;
}

.checkbox-group {
  display: flex;
  gap: 1rem;
}

.checkbox-group label {
  font-weight: normal;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

button {
  width: 100%;
  padding: 0.75rem;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
}

button:disabled {
  background-color: #a5d6a7;
  cursor: not-allowed;
}

.error-text {
  color: red;
  font-size: 0.875rem;
  margin-top: 0.5rem;
}
</style>