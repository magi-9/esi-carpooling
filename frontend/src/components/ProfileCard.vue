<!--
eslint-disable vue/no-mutating-props
-->
<template>
  <n-card>
    <n-space vertical>
      <n-h2>Personal Information</n-h2>

      <!-- Loading State -->
      <n-spin v-if="loading" />

      <!-- Profile Form -->
      <n-form v-if="!loading" :model="profileForm" :rules="profileRules">
        <n-form-item label="Email" path="email">
          <n-input v-model:value="profileForm.email" readonly :disabled="true" />
        </n-form-item>

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
          <n-space>
            <n-tag :type="getStatusColor(profile.driverStatus)">
              {{ profile.driverStatus || 'NONE' }}
            </n-tag>
            <n-button
              v-if="profile.driverStatus === 'NONE'"
              type="primary"
              size="small"
              @click="requestDriverVerification"
              :loading="requestingVerification"
            >
              Request Verification
            </n-button>
          </n-space>
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
</template>

<script setup>
import { NSpace, NCard, NH2, NForm, NFormItem, NInput, NTag, NSpin, NAlert } from 'naive-ui';

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const props = defineProps({
  profileForm: {
    type: Object,
    required: true
  },
  profileRules: {
    type: Object,
    required: true
  },
  profile: {
    type: Object,
    required: true
  },
  loading: {
    type: Boolean,
    required: true
  },
  successMessage: {
    type: String,
    default: ''
  },
  errorMessage: {
    type: String,
    default: ''
  },
  saveProfileChanges: {
    type: Function,
    required: true
  },
  requestDriverVerification: {
    type: Function,
    required: true
  },
  requestingVerification: {
    type: Boolean,
    default: false
  }
});

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
</script>

<style scoped>
.success-message,
.error-message {
  margin: 10px 0;
}
</style>
