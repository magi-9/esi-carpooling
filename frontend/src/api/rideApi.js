import axios from 'axios';

const client = axios.create({
  baseURL: import.meta.env.VITE_GATEWAY_API_URL || 'http://localhost:8080'
});

const authHeader = () => ({
  Authorization: `Bearer ${localStorage.getItem('jwt_token') || 'dev-token'}`
});

export const createRide = (rideData) => client.post('/api/rides', rideData, { headers: authHeader() });
export const getRides = () => client.get('/api/rides', { headers: authHeader() });
export const getRide = (rideId) => client.get(`/api/rides/${rideId}`, { headers: authHeader() });
export const updateRide = (rideId, rideData) => client.put(`/api/rides/${rideId}`, rideData, { headers: authHeader() });
export const deleteRide = (rideId) => client.delete(`/api/rides/${rideId}`, { headers: authHeader() });
