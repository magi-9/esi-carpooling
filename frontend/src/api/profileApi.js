import { useAuthStore } from '@/stores/auth';
import axios from 'axios';

const client = axios.create({
  baseURL: import.meta.env.VITE_GATEWAY_API_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Request Interceptor: Attach the token to every request
client.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore();
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Handle 401 errors
client.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response && error.response.status === 401) {
      const authStore = useAuthStore();
      authStore.clearToken();
    }
    return Promise.reject(error);
  }
);

export const getProfile = (userId) => client.get(`/api/profiles/${userId}`);

export const updateProfile = (userId, data) => client.put(`/api/profiles/${userId}`, data);

export const getVehicles = (userId) => client.get(`/api/profiles/${userId}/vehicles`);

export const addVehicle = (userId, data) => client.post(`/api/profiles/${userId}/vehicles`, data);

export const getVerifiedVehicles = (userId) => client.get(`/api/profiles/${userId}/vehicles/verified`);

export default {
  getProfile,
  updateProfile,
  getVehicles,
  addVehicle,
  getVerifiedVehicles
};
