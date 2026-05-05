<template>
  <div style="padding:24px;max-width:800px;margin:0 auto;text-align:center">
    <h1 style="color:#2c3e50;margin-bottom:16px">Welcome to Carpooling</h1>
    <p style="color:#666;font-size:18px;margin-bottom:32px">
      Find rides, share journeys, and travel together with our carpooling platform.
    </p>

    <div style="display:flex;gap:20px;justify-content:center;margin-bottom:48px">
      <div style="background:#f8f9fa;padding:24px;border-radius:8px;flex:1;max-width:300px">
        <h3 style="color:#2c3e50;margin-bottom:12px">🚗 Find a Ride</h3>
        <p style="color:#666;margin-bottom:16px">
          Search for available rides in your area and book your seat.
        </p>
        <RouterLink
          to="/search"
          style="display:inline-block;background:#2196F3;color:white;padding:12px 24px;text-decoration:none;border-radius:4px"
        >
          Search Rides
        </RouterLink>
      </div>

      <div style="background:#f8f9fa;padding:24px;border-radius:8px;flex:1;max-width:300px">
        <h3 style="color:#2c3e50;margin-bottom:12px">💰 Make Payments</h3>
        <p style="color:#666;margin-bottom:16px">
          Secure payment processing for your carpooling transactions.
        </p>
        <RouterLink
          v-if="authStore.isAuthenticated"
          to="/payments/new"
          style="display:inline-block;background:#4CAF50;color:white;padding:12px 24px;text-decoration:none;border-radius:4px"
        >
          New Payment
        </RouterLink>
        <RouterLink
          v-else
          to="/login"
          style="display:inline-block;background:#4CAF50;color:white;padding:12px 24px;text-decoration:none;border-radius:4px"
        >
          Login to Pay
        </RouterLink>
      </div>
    </div>

    <div v-if="!authStore.isAuthenticated" style="background:#e3f2fd;padding:24px;border-radius:8px;margin-bottom:32px">
      <h3 style="color:#2c3e50;margin-bottom:12px">Join Our Community</h3>
      <p style="color:#666;margin-bottom:16px">
        Create an account to start carpooling and connect with drivers and passengers.
      </p>
      <div style="display:flex;gap:12px;justify-content:center">
        <RouterLink
          to="/register"
          style="background:#2196F3;color:white;padding:12px 24px;text-decoration:none;border-radius:4px"
        >
          Register Now
        </RouterLink>
        <RouterLink
          to="/login"
          style="background:#666;color:white;padding:12px 24px;text-decoration:none;border-radius:4px"
        >
          Sign In
        </RouterLink>
      </div>
    </div>

    <div v-else style="background:#e8f5e8;padding:24px;border-radius:8px">
      <h3 style="color:#2c3e50;margin-bottom:12px">Welcome back!</h3>
      <p style="color:#666;margin-bottom:16px">
        Ready to find your next ride or manage your payments?
      </p>
      <div style="display:flex;gap:12px;justify-content:center">
        <RouterLink
          to="/search"
          style="background:#2196F3;color:white;padding:12px 24px;text-decoration:none;border-radius:4px"
        >
          Find Rides
        </RouterLink>
        <RouterLink
          to="/payments/new"
          style="background:#4CAF50;color:white;padding:12px 24px;text-decoration:none;border-radius:4px"
        >
          Make Payment
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
</script>