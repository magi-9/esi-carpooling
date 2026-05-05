import authApi from '@/services/authApi';
import { useAuthStore } from '@/stores/auth';
import { createPinia } from 'pinia';
import { createApp } from 'vue';
import App from './App.vue';
import router from './router';

// 1. Import Naive UI and Fonts
import naive from 'naive-ui';
import 'vfonts/FiraCode.css'; // Monospace Font
import 'vfonts/Inter.css'; // General Font

const app = createApp(App);
const pinia = createPinia();

app.use(pinia);
app.use(naive);

// Re-initialize the refresh timer if a token already exists on load
const authStore = useAuthStore();
if (authStore.token) {
  // Validate the token on startup
  authApi
    .get('/api/auth/validate')
    .then(() => {
      authStore.startRefreshTimer();
    })
    .catch((err) => {
      console.warn('Stored token is invalid, clearing:', err);
      authStore.clearToken();
    });
}

app.use(router);
app.mount('#app');
