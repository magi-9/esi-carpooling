<template>
  <div class="login-container">
    <h2>Welcome Back</h2>
    
    <form @submit.prevent="handleLogin">
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
          placeholder="Enter your password"
          required 
        />
      </div>

      <!-- Submit Button -->
      <button type="submit" :disabled="loading">
        {{ loading ? 'Signing in...' : 'Sign In' }}
      </button>

      <!-- API Error Display -->
      <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
    </form>
    
    <div class="register-link">
      <p>Don't have an account? <router-link to="/register">Register here</router-link></p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth'; // Ensure your Vite alias is configured

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
    
    // Redirect to the protected area of your application
    router.push('/search'); 
    
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

<style scoped>
.login-container {
  max-width: 400px;
  margin: 0 auto;
  padding: 2rem;
  border: 1px solid #ccc;
  border-radius: 8px;
  background-color: #f9f9f9;
}

h2 {
  text-align: center;
  margin-bottom: 1.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: bold;
}

.form-group input {
  width: 100%;
  padding: 0.75rem;
  box-sizing: border-box;
  border: 1px solid #ccc;
  border-radius: 4px;
}

button {
  width: 100%;
  padding: 0.75rem;
  background-color: #2196F3; /* A different color from register to distinguish */
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: bold;
}

button:disabled {
  background-color: #90caf9;
  cursor: not-allowed;
}

.error-text {
  color: #d32f2f;
  font-size: 0.875rem;
  margin-top: 1rem;
  text-align: center;
}

.register-link {
  margin-top: 1.5rem;
  text-align: center;
  font-size: 0.9rem;
}

.register-link a {
  color: #2196F3;
  text-decoration: none;
}

.register-link a:hover {
  text-decoration: underline;
}
</style>