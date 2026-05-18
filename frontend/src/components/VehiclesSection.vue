<!--
eslint-disable vue/no-mutating-props
-->
<template>
  <n-card>
    <n-space vertical>
      <n-space justify="space-between" align="center">
        <n-h2 style="margin: 0">Vehicles</n-h2>
        <n-button type="primary" @click="showModal = true">Add Vehicle</n-button>
      </n-space>

      <!-- Vehicles List -->
      <div v-if="vehicles.length > 0" class="vehicles-list">
        <n-space vertical>
          <div v-for="vehicle in vehicles" :key="vehicle.vehicleId" class="vehicle-item">
            <n-card size="small">
              <n-space justify="space-between" align="center">
                <div>
                  <div>
                    <strong>{{ vehicle.make }} {{ vehicle.model }}</strong>
                  </div>
                  <div class="license-plate">
                    {{ vehicle.licensePlate }}
                  </div>
                </div>
                <div style="display:flex; gap:8px; align-items:center">
                  <n-tag :type="getVerificationColor(vehicle.verificationStatus)">
                    {{ vehicle.verificationStatus }}
                  </n-tag>
                  <n-button size="small" primary @click="retryVehicle(vehicle)" :loading="retrying[vehicle.vehicleId]" v-if="vehicle.verificationStatus === 'FAILED'">Retry</n-button>
                </div>
              </n-space>
              <div v-if="vehicle.verificationStatus !== 'SUCCESS'" style="margin-top: 12px">
                <VehicleValidation :vehicle="vehicle" />
              </div>
            </n-card>
          </div>
        </n-space>
      </div>

      <div v-else class="no-vehicles">
        <n-empty description="No vehicles added yet" />
      </div>

      <!-- Add Vehicle Modal -->
      <n-modal
        v-model:show="showModal"
        preset="card"
        title="Add New Vehicle"
        style="width: 500px; max-width: 90vw"
      >
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

          <n-button type="primary" @click="submitForm" :loading="addingVehicle">
            Add Vehicle
          </n-button>
        </n-form>
      </n-modal>
    </n-space>
  </n-card>
</template>

<script setup>
import { ref } from 'vue';
import {
  NSpace,
  NCard,
  NH2,
  NForm,
  NFormItem,
  NInput,
  NButton,
  NEmpty,
  NTag,
  NModal
} from 'naive-ui';
import VehicleValidation from '@/components/VehicleValidation.vue';
import { getValidationsByVehicle, retryValidation } from '@/api/validationApi';

const props = defineProps({
  vehicles: {
    type: Array,
    required: true
  },
  vehicleForm: {
    type: Object,
    required: true
  },
  vehicleRules: {
    type: Object,
    required: true
  },
  addingVehicle: {
    type: Boolean,
    required: true
  },
  addNewVehicle: {
    type: Function,
    required: true
  }
});

// Methods
const showModal = ref(false);
const retrying = ref({});

const submitForm = async () => {
  await props.addNewVehicle();
  // Close the modal only if the parent successfully cleared the form
  if (!props.vehicleForm.make && !props.vehicleForm.model && !props.vehicleForm.licensePlate) {
    showModal.value = false;
  }
};

const getVerificationColor = (status) => {
  const colors = {
    SUCCESS: 'success',
    PENDING: 'warning',
    FAILED: 'error'
  };
  return colors[status] || 'default';
};

async function retryVehicle(vehicle) {
  try {
    retrying.value[vehicle.vehicleId] = true;
    const resp = await getValidationsByVehicle(vehicle.vehicleId);
    const list = resp.data || [];
    // find a failed request (isApproved === false)
    const failed = list.find((r) => r.isApproved === false);
    if (!failed) {
      alert('No failed validation request found to retry.');
      retrying.value[vehicle.vehicleId] = false;
      return;
    }

    await retryValidation(failed.requestId);
    // Optimistically set to pending so UI reflects retry
    vehicle.verificationStatus = 'PENDING';
  } catch (e) {
    console.error('Retry failed', e);
  } finally {
    retrying.value[vehicle.vehicleId] = false;
  }
}
</script>

<style scoped>
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
</style>
