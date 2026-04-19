<template>
  <div style="padding:24px;max-width:900px;margin:0 auto">
    <h1>Find a Ride</h1>
    <SearchForm @search="handleSearch" />

    <div v-if="loading" style="margin-top:24px;color:#666">Searching...</div>
    <div v-if="error" style="margin-top:24px;color:red">{{ error }}</div>

    <div v-if="results.length > 0" style="margin-top:24px">
      <h2>{{ results.length }} ride(s) found</h2>
      <RideCard
        v-for="rec in results"
        :key="rec.recommendationId"
        :recommendation="rec"
        @book="handleBook"
      />
    </div>

    <div v-else-if="!loading && searched" style="margin-top:24px;color:#666">
      No rides found matching your criteria.
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import SearchForm from '../components/SearchForm.vue'
import RideCard from '../components/RideCard.vue'
import { searchRides } from '../api/discoveryApi'

const router = useRouter()
const results = ref([])
const loading = ref(false)
const error = ref('')
const searched = ref(false)

async function handleSearch(params) {
  loading.value = true
  error.value = ''
  searched.value = true
  try {
    const response = await searchRides(params)
    results.value = response.data.recommendations || []
  } catch (e) {
    error.value = e.response?.data?.error || 'Failed to search rides. Is the backend running?'
    results.value = []
  } finally {
    loading.value = false
  }
}

function handleBook(rideId) {
  // Navigate to payment page; in real system bookingId comes from Booking Service
  // For demo, use rideId as a stand-in for bookingId
  router.push({ path: '/payments/new', query: { bookingId: rideId } })
}
</script>
