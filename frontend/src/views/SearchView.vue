<template>
  <div style="padding: 24px; max-width: 900px; margin: 0 auto">
    <h1>Find a Ride</h1>
    <SearchForm @search="handleSearch" />

    <div v-if="loading" style="margin-top: 24px; color: #666">Searching...</div>
    <div v-if="error" style="margin-top: 24px; color: red">{{ error }}</div>

    <div v-if="results.length > 0" style="margin-top: 24px">
      <h2>{{ results.length }} ride(s) found</h2>
      <RideCard
        v-for="rec in results"
        :key="rec.recommendationId"
        :recommendation="rec"
        @book="handleBook"
      />
    </div>

    <div v-else-if="!loading && searched" style="margin-top: 24px; color: #666">
      No rides found matching your criteria.
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import SearchForm from '../components/SearchForm.vue';
import RideCard from '../components/RideCard.vue';
import { searchRides } from '../api/discoveryApi';
import { createBooking, getRide } from '@/api/rideApi';

const router = useRouter();
const results = ref([]);
const loading = ref(false);
const error = ref('');
const searched = ref(false);

async function handleSearch(params) {
  loading.value = true;
  error.value = '';
  searched.value = true;
  try {
    const response = await searchRides(params);
    const recommendations = response.data.recommendations || [];

    // Fetch full ride details for each recommendation
    const enriched = await Promise.all(
      recommendations.map(async (rec) => {
        try {
          const rideResp = await getRide(rec.rideId);
          return { ...rec, ride: rideResp.data };
        } catch {
          return { ...rec, ride: null };
        }
      })
    );

    results.value = enriched;
  } catch (e) {
    error.value = e.response?.data?.error || 'Failed to search rides. Is the backend running?';
    results.value = [];
  } finally {
    loading.value = false;
  }
}

async function handleBook(recommendation) {
  try {
    const rideId = recommendation.rideId;
    const bookingResp = await createBooking(rideId);
    const bookingId = String(bookingResp.data);
    const ride = recommendation.ride || {};

    router.push({
      path: '/payments/new',
      query: {
        bookingId,
        amount: ride.seatPriceAmount || 25,
        currency: ride.seatPriceCurrency || 'EUR',
        payeeId: ride.driverId || 'driver'
      }
    });
  } catch (e) {
    error.value = e.response?.data?.error || 'Failed to create booking for this ride.';
  }
}
</script>
