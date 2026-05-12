import axios from 'axios';

const client = axios.create({
  baseURL: import.meta.env.VITE_GATEWAY_API_URL || 'http://localhost:8080'
});

const authHeader = () => ({
  Authorization: `Bearer ${localStorage.getItem('jwt_token') || 'dev-token'}`
});

export const getReviews = (params) => client.get('/api/reviews', { params, headers: authHeader() });
export const getReview = (reviewId) => client.get(`/api/reviews/${reviewId}`, { headers: authHeader() });
export const createReview = (reviewData) => client.post('/api/reviews', reviewData, { headers: authHeader() });
export const deleteReview = (reviewId) => client.delete(`/api/reviews/${reviewId}`, { headers: authHeader() });
