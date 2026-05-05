<template>
  <div class="profile-container">
    <n-space vertical>
      <!-- Header -->
      <n-h1>My Profile</n-h1>

      <!-- Profile Card -->
      <ProfileCard
        :profileForm="profileForm"
        :profileRules="profileRules"
        :profile="profile"
        :loading="loading"
        :successMessage="successMessage"
        :errorMessage="errorMessage"
        :saveProfileChanges="saveProfileChanges"
      />

      <!-- Vehicles Section -->
      <VehiclesSection
        :vehicles="vehicles"
        :vehicleForm="vehicleForm"
        :vehicleRules="vehicleRules"
        :addingVehicle="addingVehicle"
        :addNewVehicle="addNewVehicle"
      />
    </n-space>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { getProfile, updateProfile, getVehicles, addVehicle } from '@/api/profileApi';
import ProfileCard from '@/components/ProfileCard.vue';
import VehiclesSection from '@/components/VehiclesSection.vue';
import { NSpace, NH1 } from 'naive-ui';

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

const originalProfile = ref({
  firstName: '',
  lastName: '',
  phoneNumber: ''
});

const profileForm = ref({
  firstName: '',
  lastName: '',
  phoneNumber: '',
  email: ''
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
    required: false,
    message: 'First name is required',
    trigger: 'blur'
  },
  lastName: {
    required: false,
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
const fetchProfile = async () => {
  try {
    loading.value = true;
    const userId = authStore.currentUserId;

    if (!userId) {
      throw new Error('User ID not found');
    }

    const response = await getProfile(userId);
    profile.value = response.data;
    profile.value.driverStatus = authStore.getRolesFromToken(authStore.token).includes('DRIVER')
      ? 'VERIFIED'
      : 'NONE';
    profileForm.value = {
      firstName: response.data.firstName,
      lastName: response.data.lastName,
      phoneNumber: response.data.phoneNumber || '',
      email: authStore.getEmailFromToken(authStore.token) || ''
    };

    originalProfile.value = {
      firstName: profileForm.value.firstName,
      lastName: profileForm.value.lastName,
      phoneNumber: profileForm.value.phoneNumber
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
  // Check if any values have changed
  if (
    profileForm.value.firstName === originalProfile.value.firstName &&
    profileForm.value.lastName === originalProfile.value.lastName &&
    profileForm.value.phoneNumber === originalProfile.value.phoneNumber
  ) {
    return; // No changes, skip update
  }

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

    // Update original values
    originalProfile.value = {
      firstName: profileForm.value.firstName,
      lastName: profileForm.value.lastName,
      phoneNumber: profileForm.value.phoneNumber
    };

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
