import axios from 'axios';

const client = axios.create({
  baseURL: import.meta.env.VITE_GATEWAY_API_URL || 'http://localhost:8080'
});

const authHeader = () => ({
  Authorization: `Bearer ${localStorage.getItem('jwt_token') || 'dev-token'}`
});

export const searchRides = (params) => client.get('/api/search', { params, headers: authHeader() });
