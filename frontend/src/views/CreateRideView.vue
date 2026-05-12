<template>
  <n-space vertical size="large" style="padding: 24px; max-width: 800px; margin: 0 auto">
    <n-h1>Create a New Ride</n-h1>
    <n-text style="color: #666">
      Offer a ride to passengers. Fill in the details below.
    </n-text>

    <n-form @submit.prevent="handleCreateRide" :show-label="true" ref="formRef">
      <n-space vertical size="large">
        <!-- Start Address -->
        <n-form-item label="Start Address" required>
          <n-input
            v-model:value="form.startAddress"
            placeholder="e.g., 123 Main St, Tallinn"
            :disabled="loading"
            clearable
          />
        </n-form-item>

        <!-- End Address -->
        <n-form-item label="End Address" required>
          <n-input
            v-model:value="form.endAddress"
            placeholder="e.g., 456 Oak Ave, Tartu"
            :disabled="loading"
            clearable
          />
        </n-form-item>

        <!-- Departure Date & Time -->
        <n-form-item label="Departure Date & Time" required>
          <n-date-picker
            v-model:value="form.rideStartDate"
            type="datetime"
            placeholder="Select departure time"
            :disabled="loading"
            :is-date-disabled="isDateDisabled"
            style="width: 100%"
          />
        </n-form-item>

        <!-- Vehicle Selection -->
        <n-form-item label="Vehicle" required>
          <n-select
            v-model:value="form.vehicleId"
            :options="vehicleOptions"
            :loading="vehiclesLoading"
            :disabled="loading || vehiclesLoading"
            placeholder="Select your vehicle"
            clearable
            @update:value="handleVehicleChange"
          />
          <n-alert v-if="vehiclesError" type="warning" size="small" style="margin-top: 8px">
            {{ vehiclesError }}
          </n-alert>
          <n-alert v-else-if="!vehiclesLoading && vehicles.length === 0" type="info" size="small" style="margin-top: 8px">
            No verified vehicles found. Please add and verify a vehicle in your profile first.
          </n-alert>
          <n-text v-else depth="3" style="font-size: 12px; margin-top: 4px">
            Select from your registered vehicles.
          </n-text>
        </n-form-item>

        <!-- Available Seats -->
        <n-form-item label="Available Seats" required>
          <n-input-number
            v-model:value="form.availableSeats"
            :min="1"
            :max="8"
            placeholder="Number of seats"
            :disabled="loading"
            style="width: 100%"
          />
        </n-form-item>

        <!-- Price Per Seat -->
        <n-form-item label="Price Per Seat" required>
          <n-space align="center">
            <n-input-number
              v-model:value="form.seatPriceAmount"
              :min="0"
              :precision="2"
              placeholder="Amount"
              :disabled="loading"
              style="width: 200px"
            />
            <n-select
              v-model:value="form.seatPriceCurrency"
              :options="currencyOptions"
              :disabled="loading"
              style="width: 100px"
            />
          </n-space>
        </n-form-item>

        <!-- Submit Button -->
        <n-space vertical size="small">
          <n-button type="primary" :loading="loading" block attr-type="submit" size="large">
            {{ loading ? 'Creating Ride...' : 'Create Ride' }}
          </n-button>

          <n-alert v-if="errorMessage" type="error" :show-icon="false">
            {{ errorMessage }}
          </n-alert>

          <n-alert v-if="successMessage" type="success" :show-icon="false">
            {{ successMessage }}
          </n-alert>
        </n-space>
      </n-space>
    </n-form>
  </n-space>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { createRide } from '@/api/rideApi';
import { getVerifiedVehicles } from '@/api/profileApi';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();
const loading = ref(false);
const errorMessage = ref('');
const successMessage = ref('');
const vehiclesLoading = ref(false);
const vehicles = ref([]);
const vehiclesError = ref('');

const currencyOptions = [
  { label: 'EUR', value: 'EUR' },
  { label: 'USD', value: 'USD' }
];

const vehicleOptions = computed(() => {
  return vehicles.value.map(v => ({
    label: `${v.make} ${v.model} (${v.licensePlate}) - ${v.seats} seats`,
    value: v.vehicleId,
    seats: v.seats
  }));
});

const form = ref({
  startAddress: '',
  endAddress: '',
  rideStartDate: null,
  vehicleId: null,
  availableSeats: 3,
  seatPriceAmount: 15.00,
  seatPriceCurrency: 'EUR'
});

// Fetch user's vehicles on mount
onMounted(async () => {
  const userId = authStore.currentUserId;
  if (!userId) {
    vehiclesError.value = 'User ID not found. Please log in again.';
    return;
  }

  vehiclesLoading.value = true;
  vehiclesError.value = '';

  try {
    const response = await getVerifiedVehicles(userId);
    vehicles.value = response.data || [];
  } catch (error) {
    console.error('Failed to fetch vehicles:', error);
    vehiclesError.value = 'Failed to load your vehicles. Please refresh or check your profile.';
  } finally {
    vehiclesLoading.value = false;
  }
});

// Update available seats when vehicle is selected
const handleVehicleChange = (vehicleId) => {
  const selectedVehicle = vehicles.value.find(v => v.vehicleId === vehicleId);
  if (selectedVehicle && selectedVehicle.seats) {
    form.value.availableSeats = Math.min(3, selectedVehicle.seats - 1); // Driver takes 1 seat
  }
};

// Disable dates before today (prevent selecting past dates)
const isDateDisabled = (timestamp) => {
  const now = Date.now();
  return timestamp < now;
};

const handleCreateRide = async () => {
  errorMessage.value = '';
  successMessage.value = '';

  // Validation
  if (!form.value.startAddress || !form.value.endAddress) {
    errorMessage.value = 'Please enter both start and end addresses.';
    return;
  }

  if (!form.value.rideStartDate) {
    errorMessage.value = 'Please select a departure date and time.';
    return;
  }

  if (!form.value.vehicleId) {
    errorMessage.value = 'Please select a vehicle.';
    return;
  }

  loading.value = true;

  try {
    // Convert timestamp to ISO string for backend
    const rideData = {
      ...form.value,
      rideStartDate: new Date(form.value.rideStartDate).toISOString()
    };

    const response = await createRide(rideData);
    successMessage.value = `Ride created successfully! Ride ID: ${response.data}`;

    // Reset form
    form.value = {
      startAddress: '',
      endAddress: '',
      rideStartDate: null,
      vehicleId: null,
      availableSeats: 3,
      seatPriceAmount: 15.00,
      seatPriceCurrency: 'EUR'
    };

    // Optional: redirect to search or home after a delay
    setTimeout(() => {
      router.push('/search');
    }, 2000);

  } catch (error) {
    console.error('Create ride failed:', error);
    if (error.response) {
      if (error.response.status === 401 || error.response.status === 403) {
        errorMessage.value = 'You must be logged in as a DRIVER to create a ride.';
      } else if (error.response.status === 503) {
        errorMessage.value = 'Service temporarily unavailable. Please try again later.';
      } else {
        errorMessage.value = error.response.data?.message || 'Failed to create ride. Please check your inputs.';
      }
    } else {
      errorMessage.value = 'Network error. Please check your connection.';
    }
  } finally {
    loading.value = false;
  }
};
</script>
