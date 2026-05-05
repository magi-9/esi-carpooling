import axios from 'axios';

const client = axios.create({
  baseURL: import.meta.env.VITE_PAYMENT_API_URL || 'http://localhost:8081'
});

const authHeader = () => ({
  Authorization: `Bearer ${localStorage.getItem('token') || 'dev-token'}`
});

export const initiatePayment = (data) => client.post('/payments', data, { headers: authHeader() });

export const getPayment = (paymentId) =>
  client.get(`/payments/${paymentId}`, { headers: authHeader() });

export const completePayment = (paymentId) =>
  client.patch(`/payments/${paymentId}/complete`, {}, { headers: authHeader() });

export const requestRefund = (paymentId, data) =>
  client.post(`/payments/${paymentId}/refunds`, data, { headers: authHeader() });

export const getRefund = (paymentId) =>
  client.get(`/payments/${paymentId}/refunds`, { headers: authHeader() });
