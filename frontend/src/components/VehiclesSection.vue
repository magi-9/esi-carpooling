<!--
eslint-disable vue/no-mutating-props
-->
<template>
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
</template>

<script setup>
import {
  NSpace,
  NCard,
  NH2,
  NH3,
  NForm,
  NFormItem,
  NInput,
  NButton,
  NTag,
  NEmpty,
  NDivider
} from 'naive-ui';

// eslint-disable-next-line @typescript-eslint/no-unused-vars
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
