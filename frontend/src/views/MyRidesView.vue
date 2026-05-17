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
        <n-space align="center">
          <n-tag :type="statusType(ride.status)">{{ ride.status }}</n-tag>
          <template v-if="ride.status !== 'COMPLETED' && ride.status !== 'CANCELLED'">
            <n-button v-if="ride.status === 'PENDING' || ride.status === 'CONFIRMED'" size="small" type="success" :loading="completing === ride.rideId" @click="handleMarkComplete(ride)">Mark Complete</n-button>
            <n-button size="small" @click="openEditModal(ride)">Edit</n-button>
            <n-popconfirm @positive-click="handleDeleteRide(ride.rideId)">
              <template #trigger>
                <n-button size="small" type="error">Delete</n-button>
              </template>
              Cancel this ride and all its bookings?
            </n-popconfirm>
          </template>
        </n-space>
      </template>

      <n-space vertical>
        <n-space>
          <n-tag>📅 {{ formatDate(ride.rideStartDate) }}</n-tag>
          <n-tag>💺 {{ ride.availableSeats }} seats</n-tag>
          <n-tag>💰 {{ ride.seatPriceAmount }} {{ ride.seatPriceCurrency }}</n-tag>
        </n-space>

        <n-divider style="margin: 12px 0" />

        <n-h4 style="margin: 0 0 8px">Bookings ({{ rideBookings(ride.rideId).length }})</n-h4>
        <template v-if="rideBookings(ride.rideId).length === 0">
          <n-text depth="3" style="margin-bottom: 8px">No bookings yet.</n-text>
        </template>
        <n-card v-for="booking in rideBookings(ride.rideId)" :key="booking.bookingId" size="small" style="margin-bottom: 4px">
          <n-space align="center">
            <n-tag :type="bookingStatusType(booking.status)" size="small">{{ booking.status }}</n-tag>
            <n-text depth="3">Passenger: {{ booking.passengerId.slice(0, 8) }}...</n-text>
          </n-space>
        </n-card>

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

    <!-- Edit Ride Modal -->
    <n-modal v-model:show="showEditModal">
      <n-card
        style="width: 500px"
        title="Edit Ride"
        :bordered="false"
        size="huge"
        role="dialog"
        aria-modal="true"
      >
        <n-space vertical>
          <n-form-item label="Date & Time">
            <n-date-picker v-model:value="editForm.rideStartDate" type="datetime" :default-value="editForm.rideStartDate ? new Date(editForm.rideStartDate).getTime() : Date.now()" />
          </n-form-item>
          <n-form-item label="Price (EUR)">
            <n-input-number v-model:value="editForm.seatPriceAmount" :min="0" :step="1" />
          </n-form-item>
          <n-form-item label="Status">
            <n-select v-model:value="editForm.status" :options="statusOptions" />
          </n-form-item>
          <n-alert v-if="editError" type="error">{{ editError }}</n-alert>
          <n-space justify="end">
            <n-button @click="showEditModal = false">Cancel</n-button>
            <n-button type="primary" :loading="saving" @click="handleEditRide">Save</n-button>
          </n-space>
        </n-space>
      </n-card>
    </n-modal>
  </n-space>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { getRides, updateRide, deleteRide } from '@/api/rideApi';
import { getReviews } from '@/api/reviewApi';

const authStore = useAuthStore();
const loading = ref(true);
const error = ref('');
const allRides = ref([]);
const allReviews = ref([]);
const allBookings = ref({});

const showEditModal = ref(false);
const editingRideId = ref(null);
const editError = ref('');
const saving = ref(false);
const completing = ref(null);
const editForm = ref({ rideStartDate: null, seatPriceAmount: 0, status: 'PENDING' });

const statusOptions = [
  { label: 'Pending', value: 'PENDING' },
  { label: 'Confirmed', value: 'CONFIRMED' },
  { label: 'Completed', value: 'COMPLETED' },
  { label: 'Cancelled', value: 'CANCELLED' }
];

const driverRides = computed(() => {
  const userId = authStore.currentUserId;
  if (!userId) return [];
  return allRides.value.filter(r => r.driverId === userId);
});

const rideReviews = (rideId) => {
  return allReviews.value.filter(r => r.rideId === rideId);
};

const rideBookings = (rideId) => {
  return allBookings.value[rideId] || [];
};

const bookingStatusType = (status) => {
  const map = { PENDING: 'warning', CONFIRMED: 'info', COMPLETED: 'success', CANCELLED: 'error' };
  return map[status] || 'default';
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

const openEditModal = (ride) => {
  editingRideId.value = ride.rideId;
  editForm.value = {
    rideStartDate: new Date(ride.rideStartDate).getTime(),
    seatPriceAmount: ride.seatPriceAmount,
    status: ride.status
  };
  editError.value = '';
  showEditModal.value = true;
};

const handleEditRide = async () => {
  editError.value = '';
  saving.value = true;
  try {
    const dto = {
      rideStartDate: new Date(editForm.value.rideStartDate).toISOString().slice(0, 19),
      seatPriceAmount: editForm.value.seatPriceAmount,
      seatPriceCurrency: 'EUR',
      status: editForm.value.status
    };
    await updateRide(editingRideId.value, dto);
    showEditModal.value = false;
    await loadData();
  } catch (e) {
    editError.value = e.response?.data || 'Failed to update ride';
  } finally {
    saving.value = false;
  }
};

const handleMarkComplete = async (ride) => {
  completing.value = ride.rideId;
  try {
    await updateRide(ride.rideId, {
      rideStartDate: new Date(ride.rideStartDate).toISOString().slice(0, 19),
      seatPriceAmount: ride.seatPriceAmount,
      seatPriceCurrency: ride.seatPriceCurrency || 'EUR',
      status: 'COMPLETED'
    });
    await loadData();
  } catch (e) {
    error.value = e.response?.data || 'Failed to complete ride';
  } finally {
    completing.value = null;
  }
};

const handleDeleteRide = async (rideId) => {
  try {
    await deleteRide(rideId);
    await loadData();
  } catch (e) {
    error.value = e.response?.data || 'Failed to delete ride';
  }
};

const loadData = async () => {
  const token = localStorage.getItem('jwt_token');
  const [ridesResp, reviewsResp] = await Promise.allSettled([
    getRides(),
    getReviews()
  ]);
  if (ridesResp.status === 'fulfilled') {
    allRides.value = ridesResp.value.data || [];
    // Fetch bookings for each ride
    for (const ride of allRides.value) {
      try {
        const resp = await fetch(`${import.meta.env.VITE_GATEWAY_API_URL || 'http://localhost:8080'}/api/bookings/ride/${ride.rideId}`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        if (resp.ok) allBookings.value[ride.rideId] = await resp.json();
      } catch {}
    }
  }
  if (reviewsResp.status === 'fulfilled') allReviews.value = reviewsResp.value.data || [];
};

onMounted(async () => {
  try {
    loading.value = true;
    await loadData();
  } catch (e) {
    error.value = 'Failed to load rides. Please try again.';
  } finally {
    loading.value = false;
  }
});
</script>
