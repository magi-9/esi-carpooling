<template>
  <div class="profile-container">
    <n-space vertical>
      <!-- Header -->
      <n-h1>My Profile</n-h1>

      <!-- Profile Card -->
      <n-card>
        <n-space vertical>
          <n-h2>Personal Information</n-h2>

          <!-- Loading State -->
          <n-spin v-if="loading" />

          <!-- Profile Form -->
          <n-form v-if="!loading" :model="profileForm" :rules="profileRules">
            <n-form-item label="First Name" path="firstName">
              <n-input
                v-model:value="profileForm.firstName"
                placeholder="Enter first name"
                @blur="saveProfileChanges"
              />
            </n-form-item>

            <n-form-item label="Last Name" path="lastName">
              <n-input
                v-model:value="profileForm.lastName"
                placeholder="Enter last name"
                @blur="saveProfileChanges"
              />
            </n-form-item>

            <n-form-item label="Phone Number" path="phoneNumber">
              <n-input
                v-model:value="profileForm.phoneNumber"
                placeholder="Enter phone number"
                @blur="saveProfileChanges"
              />
            </n-form-item>

            <n-form-item label="Driver Status">
              <n-tag :type="getStatusColor(profile.driverStatus)">
                {{ profile.driverStatus || 'NONE' }}
              </n-tag>
            </n-form-item>
          </n-form>

          <!-- Messages -->
          <div v-if="successMessage" class="success-message">
            <n-alert type="success" closable>{{ successMessage }}</n-alert>
          </div>
          <div v-if="errorMessage" class="error-message">
            <n-alert type="error" closable>{{ errorMessage }}</n-alert>
          </div>
        </n-space>
      </n-card>

      <!-- Vehicles Section -->
      <n-card>
        <n-space vertical>
          <n-h2>Vehicles</n-h2>

          <!-- Vehicles List -->
          <div v-if="vehicles.length > 0" class="vehicles-list">
            <n-space vertical>
              <div v-for="vehicle in vehicles" :key="vehicle.vehicleId" class="vehicle-item">
                <n-card size="small">
                  <n-space>
                    <div>
                      <div>
                        <strong>{{ vehicle.make }} {{ vehicle.model }}</strong>
                      </div>
                      <div class="license-plate">
                        License Plate:
                        {{ vehicle.licensePlate }}
                      </div>
                    </div>
                    <div>
                      <n-tag v-if="vehicle.isVerified" type="success">✓ Verified</n-tag>
                      <n-tag v-else type="warning">Pending</n-tag>
                    </div>
                  </n-space>
                </n-card>
              </div>
            </n-space>
          </div>

          <div v-else class="no-vehicles">
            <n-empty description="No vehicles added yet" />
          </div>

          <!-- Add Vehicle Form -->
          <n-divider />
          <n-h3>Add New Vehicle</n-h3>

          <n-form :model="vehicleForm" :rules="vehicleRules">
            <n-form-item label="Make" path="make">
              <n-input v-model:value="vehicleForm.make" placeholder="e.g., Toyota" />
            </n-form-item>

            <n-form-item label="Model" path="model">
              <n-input v-model:value="vehicleForm.model" placeholder="e.g., Camry" />
            </n-form-item>

            <n-form-item label="License Plate" path="licensePlate">
              <n-input v-model:value="vehicleForm.licensePlate" placeholder="e.g., ABC-1234" />
            </n-form-item>

            <n-button type="primary" @click="addNewVehicle" :loading="addingVehicle">
              Add Vehicle
            </n-button>
          </n-form>
        </n-space>
      </n-card>
    </n-space>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { getProfile, updateProfile, getVehicles, addVehicle } from '@/api/profileApi';
import {
  NSpace,
  NCard,
  NH1,
  NH2,
  NH3,
  NForm,
  NFormItem,
  NInput,
  NButton,
  NTag,
  NSpin,
  NEmpty,
  NDivider,
  NAlert
} from 'naive-ui';

const router = useRouter();
const authStore = useAuthStore();

// State
const loading = ref(true);
const addingVehicle = ref(false);
const successMessage = ref('');
const errorMessage = ref('');

const profile = ref({
  userId: '',
  firstName: '',
  lastName: '',
  phoneNumber: '',
  driverStatus: 'NONE'
});

const profileForm = ref({
  firstName: '',
  lastName: '',
  phoneNumber: ''
});

const vehicles = ref([]);

const vehicleForm = ref({
  make: '',
  model: '',
  licensePlate: ''
});

// Validation Rules
const profileRules = {
  firstName: {
    required: true,
    message: 'First name is required',
    trigger: 'blur'
  },
  lastName: {
    required: true,
    message: 'Last name is required',
    trigger: 'blur'
  }
};

const vehicleRules = {
  make: {
    required: true,
    message: 'Make is required',
    trigger: 'blur'
  },
  model: {
    required: true,
    message: 'Model is required',
    trigger: 'blur'
  },
  licensePlate: {
    required: true,
    message: 'License plate is required',
    trigger: 'blur'
  }
};

// Methods
const getStatusColor = (status) => {
  const colors = {
    VERIFIED: 'success',
    PENDING: 'warning',
    REJECTED: 'error',
    NONE: 'default'
  };
  return colors[status] || 'default';
};

const fetchProfile = async () => {
  try {
    loading.value = true;
    const userId = authStore.currentUserId;

    if (!userId) {
      throw new Error('User ID not found');
    }

    const response = await getProfile(userId);
    profile.value = response.data;
    profileForm.value = {
      firstName: response.data.firstName,
      lastName: response.data.lastName,
      phoneNumber: response.data.phoneNumber || ''
    };

    // Fetch vehicles
    const vehiclesResponse = await getVehicles(userId);
    vehicles.value = vehiclesResponse.data;

    successMessage.value = 'Profile loaded successfully';
    setTimeout(() => {
      successMessage.value = '';
    }, 3000);
  } catch (error) {
    console.error('Failed to fetch profile:', error);
    errorMessage.value =
      error.response?.data?.message || 'Failed to load profile. Please try again.';
  } finally {
    loading.value = false;
  }
};

const saveProfileChanges = async () => {
  try {
    const userId = authStore.currentUserId;

    await updateProfile(userId, {
      firstName: profileForm.value.firstName,
      lastName: profileForm.value.lastName,
      phoneNumber: profileForm.value.phoneNumber
    });

    profile.value.firstName = profileForm.value.firstName;
    profile.value.lastName = profileForm.value.lastName;
    profile.value.phoneNumber = profileForm.value.phoneNumber;

    successMessage.value = 'Profile updated successfully';
    setTimeout(() => {
      successMessage.value = '';
    }, 3000);
  } catch (error) {
    console.error('Failed to update profile:', error);
    errorMessage.value =
      error.response?.data?.message || 'Failed to update profile. Please try again.';
  }
};

const addNewVehicle = async () => {
  try {
    if (!vehicleForm.value.make || !vehicleForm.value.model || !vehicleForm.value.licensePlate) {
      errorMessage.value = 'Please fill in all vehicle fields';
      return;
    }

    addingVehicle.value = true;
    const userId = authStore.currentUserId;

    const response = await addVehicle(userId, {
      make: vehicleForm.value.make,
      model: vehicleForm.value.model,
      licensePlate: vehicleForm.value.licensePlate
    });

    vehicles.value.push(response.data);

    // Reset form
    vehicleForm.value = {
      make: '',
      model: '',
      licensePlate: ''
    };

    successMessage.value = 'Vehicle added successfully';
    setTimeout(() => {
      successMessage.value = '';
    }, 3000);
  } catch (error) {
    console.error('Failed to add vehicle:', error);
    errorMessage.value =
      error.response?.data?.message || 'Failed to add vehicle. Please try again.';
  } finally {
    addingVehicle.value = false;
  }
};

// Lifecycle
onMounted(() => {
  if (!authStore.isAuthenticated) {
    router.push('/login');
    return;
  }

  fetchProfile();
});
</script>

<style scoped>
.profile-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.vehicle-item {
  margin: 10px 0;
}

.license-plate {
  color: #666;
  font-size: 0.9em;
  margin-top: 5px;
}

.no-vehicles {
  padding: 20px;
  text-align: center;
}

.success-message,
.error-message {
  margin: 10px 0;
}
</style>
