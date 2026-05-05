import axios from 'axios';

const client = axios.create({
  baseURL: import.meta.env.VITE_DISCOVERY_API_URL || 'http://localhost:8082'
});

const authHeader = () => ({
  Authorization: `Bearer ${localStorage.getItem('token') || 'dev-token'}`
});

export const searchRides = (params) => client.get('/search', { params, headers: authHeader() });

export const getSearch = (searchId) => client.get(`/search/${searchId}`, { headers: authHeader() });

export const getRecommendations = (searchId) =>
  client.get(`/search/${searchId}/recommendations`, { headers: authHeader() });
