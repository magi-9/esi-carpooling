import axios from 'axios';

const client = axios.create({
  baseURL: import.meta.env.VITE_GATEWAY_API_URL || 'http://localhost:8080'
});

const authHeader = () => ({
  Authorization: `Bearer ${localStorage.getItem('jwt_token') || 'dev-token'}`
});

export const getMyBookings = () => client.get('/api/bookings', { headers: authHeader() });
export const cancelBooking = (bookingId) => client.delete(`/api/bookings/${bookingId}`, { headers: authHeader() });
export const getMyRides = () => {
  const userId = JSON.parse(atob((localStorage.getItem('jwt_token') || '.').split('.')[1] + '=='))?.sub;
  return client.get(`/api/rides?status=COMPLETED`, { headers: authHeader() });
};
