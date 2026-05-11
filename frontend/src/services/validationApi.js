import axios from 'axios';
import { useAuthStore } from '@/stores/auth';

const validationApi = axios.create({
  baseURL: 'http://localhost:8087',
  // Let the browser set multipart boundaries when sending FormData
  headers: {
    // no default Content-Type for multipart
  }
});

// Attach token from auth store (if available)
validationApi.interceptors.request.use(
  (config) => {
    try {
      const authStore = useAuthStore();
      if (authStore.token) {
        config.headers = config.headers || {};
        config.headers.Authorization = `Bearer ${authStore.token}`;
      }
    } catch (e) {
      // store might not be available in some environments
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export async function createValidation(payload, files = []) {
  const form = new FormData();
  // Ensure the 'data' part is sent with application/json content-type
  const jsonBlob = new Blob([JSON.stringify(payload)], { type: 'application/json' });
  form.append('data', jsonBlob);
  files.forEach((f) => form.append('files', f));
  // Let the browser/axios set the multipart boundary header automatically
  return validationApi.post('/validation', form);
}

export default { createValidation };
