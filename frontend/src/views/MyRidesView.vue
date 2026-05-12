<template>
  <n-space vertical size="large" style="padding: 24px; max-width: 1000px; margin: 0 auto">
    <n-h1>My Rides</n-h1>

    <n-alert v-if="loading" type="info">Loading your rides...</n-alert>
    <n-alert v-if="error" type="error">{{ error }}</n-alert>

    <template v-if="!loading && driverRides.length === 0">
      <n-empty description="You haven't created any rides yet">
        <template #extra>
          <router-link to="/rides/create">
            <n-button type="primary">Create a Ride</n-button>
          </router-link>
        </template>
      </n-empty>
    </template>

    <n-card
      v-for="ride in driverRides"
      :key="ride.rideId"
      :title="`${ride.startAddress} → ${ride.endAddress}`"
      style="margin-bottom: 16px"
    >
      <template #header-extra>
        <n-tag :type="statusType(ride.status)">{{ ride.status }}</n-tag>
      </template>

      <n-space vertical>
        <n-space>
          <n-tag>📅 {{ formatDate(ride.rideStartDate) }}</n-tag>
          <n-tag>💺 {{ ride.availableSeats }} seats</n-tag>
          <n-tag>💰 {{ ride.seatPriceAmount }} {{ ride.seatPriceCurrency }}</n-tag>
        </n-space>

        <n-divider style="margin: 12px 0" />

        <n-h4 style="margin: 0 0 8px">Reviews ({{ rideReviews(ride.rideId).length }})</n-h4>

        <n-alert v-if="rideReviews(ride.rideId).length === 0" type="info" size="small">
          No reviews yet for this ride.
        </n-alert>

        <n-card
          v-for="review in rideReviews(ride.rideId)"
          :key="review.reviewId"
          size="small"
          style="margin-bottom: 8px"
        >
          <template #header>
            <n-space align="center">
              <n-text>{{ '★'.repeat(review.stars) + '☆'.repeat(5 - review.stars) }}</n-text>
              <n-text depth="3" style="font-size: 12px">
                {{ formatDate(review.createdAt) }}
              </n-text>
            </n-space>
          </template>
          <n-text>{{ review.comment || 'No comment' }}</n-text>
        </n-card>
      </n-space>
    </n-card>
  </n-space>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { getRides } from '@/api/rideApi';
import { getReviews } from '@/api/reviewApi';

const authStore = useAuthStore();
const loading = ref(true);
const error = ref('');
const allRides = ref([]);
const allReviews = ref([]);

const driverRides = computed(() => {
  const userId = authStore.currentUserId;
  if (!userId) return [];
  return allRides.value.filter(r => r.driverId === userId);
});

const rideReviews = (rideId) => {
  return allReviews.value.filter(r => r.rideId === rideId);
};

const statusType = (status) => {
  const map = { PENDING: 'warning', CONFIRMED: 'info', COMPLETED: 'success', CANCELLED: 'error' };
  return map[status] || 'default';
};

const formatDate = (dateStr) => {
  if (!dateStr) return 'N/A';
  return new Date(dateStr).toLocaleString('en-GB', {
    day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit'
  });
};

onMounted(async () => {
  try {
    const [ridesResp, reviewsResp] = await Promise.allSettled([
      getRides(),
      getReviews()
    ]);

    if (ridesResp.status === 'fulfilled') allRides.value = ridesResp.value.data || [];
    if (reviewsResp.status === 'fulfilled') allReviews.value = reviewsResp.value.data || [];

  } catch (e) {
    error.value = 'Failed to load rides. Please try again.';
  } finally {
    loading.value = false;
  }
});
</script>
