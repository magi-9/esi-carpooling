<template>
  <div>
    <div style="display:flex; gap:8px; align-items:center; width:100%">
      <n-tag v-if="vehicle.isVerified" type="success">✓ Verified</n-tag>
      <n-tag v-else type="warning">Pending</n-tag>
      <div style="margin-left:auto">
        <n-button size="small" secondary @click="s.open = !s.open">Validate</n-button>
      </div>
    </div>

    <div v-if="s.open" style="margin-top:12px">
      <n-divider />
      <div style="margin-top:12px">
        <n-alert v-if="s.error" type="error" :show-icon="false">{{ s.error }}</n-alert>
        <n-alert v-if="s.success" type="success" :show-icon="false">{{ s.success }}</n-alert>

        <div style="display:flex; gap:12px; align-items:center; margin-bottom:12px">
          <div style="flex:1"><strong>License</strong></div>
          <div style="flex:2; display:flex; gap:12px; align-items:center">
            <n-upload
              :file-list="s.licenseFileList"
              @update:file-list="(list) => handleUploadChange('license', list)"
              :multiple="false"
              :show-file-list="false"
            >
              <n-button size="small">Upload File</n-button>
            </n-upload>
            <div v-if="s.licenseFileName" style="color:#666; font-size:13px">{{ s.licenseFileName }}</div>
          </div>
        </div>

        <div style="display:flex; gap:12px; align-items:center; margin-bottom:12px">
          <div style="flex:1"><strong>Registration</strong></div>
          <div style="flex:2; display:flex; gap:12px; align-items:center">
            <n-upload
              :file-list="s.registrationFileList"
              @update:file-list="(list) => handleUploadChange('registration', list)"
              :multiple="false"
              :show-file-list="false"
            >
              <n-button size="small">Upload File</n-button>
            </n-upload>
            <div v-if="s.registrationFileName" style="color:#666; font-size:13px">{{ s.registrationFileName }}</div>
          </div>
        </div>

        <div style="margin-top:8px">
          <n-button type="primary" @click="submitValidation" :loading="s.loading">Submit Validation</n-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { NButton, NUpload, NAlert, NDivider, NTag } from 'naive-ui';
import { useAuthStore } from '@/stores/auth';
import { createValidation } from '@/services/validationApi';

const props = defineProps({
  vehicle: {
    type: Object,
    required: true
  }
});

const vehicle = props.vehicle;
const authStore = useAuthStore();

const s = ref({
  open: false,
  licenseFile: null,
  registrationFile: null,
  licenseFileList: [],
  registrationFileList: [],
  licenseFileName: '',
  registrationFileName: '',
  loading: false,
  error: '',
  success: ''
});

function handleUploadChange(which, list) {
  const entry = list && list.length ? list[list.length - 1] : null;
  if (which === 'license') {
    s.value.licenseFileList = list;
    s.value.licenseFile = entry?.file || null;
    s.value.licenseFileName = entry?.file?.name || '';
  } else {
    s.value.registrationFileList = list;
    s.value.registrationFile = entry?.file || null;
    s.value.registrationFileName = entry?.file?.name || '';
  }
}

async function submitValidation() {
  s.value.error = '';
  s.value.success = '';

  const userId = authStore.currentUserId || authStore.userId;
  if (!userId) {
    s.value.error = 'User not authenticated';
    return;
  }
  if (!vehicle.vehicleId) {
    s.value.error = 'Vehicle missing id';
    return;
  }

  const files = [];
  const documents = [];
  if (s.value.licenseFile) {
    files.push(s.value.licenseFile);
    documents.push({ documentType: 'license' });
  }
  if (s.value.registrationFile) {
    files.push(s.value.registrationFile);
    documents.push({ documentType: 'registration' });
  }
  if (files.length === 0) {
    s.value.error = 'Attach at least one file';
    return;
  }

  const payload = {
    userId,
    vehicleId: vehicle.vehicleId,
    documents
  };

  try {
    s.value.loading = true;
    await createValidation(payload, files);
    s.value.success = 'Validation request submitted';
    setTimeout(() => (s.value.open = false), 900);
  } catch (e) {
    s.value.error = e.response?.data?.error || 'Failed to submit validation request';
  } finally {
    s.value.loading = false;
  }
}
</script>

<style scoped></style>
