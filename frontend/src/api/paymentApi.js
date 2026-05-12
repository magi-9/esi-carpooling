import axios from 'axios';

const client = axios.create({
  baseURL: import.meta.env.VITE_GATEWAY_API_URL || 'http://localhost:8080'
});

const authHeader = () => ({
  Authorization: `Bearer ${localStorage.getItem('jwt_token') || 'dev-token'}`
});

export const initiatePayment = (data) => client.post('/api/payments', data, { headers: authHeader() });

export const getPayment = (paymentId) =>
  client.get(`/api/payments/${paymentId}`, { headers: authHeader() });

export const completePayment = (paymentId) =>
  client.patch(`/api/payments/${paymentId}/complete`, {}, { headers: authHeader() });

export const requestRefund = (paymentId, data) =>
  client.post(`/api/payments/${paymentId}/refunds`, data, { headers: authHeader() });

export const getRefund = (paymentId) =>
  client.get(`/api/payments/${paymentId}/refunds`, { headers: authHeader() });
