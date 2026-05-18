<template>
  <n-space vertical size="large" style="padding: 24px; max-width: 900px; margin: 0 auto">
    <n-h1>My Payments</n-h1>

    <n-alert v-if="loading" type="info">Loading payments...</n-alert>
    <n-alert v-if="error" type="error">{{ error }}</n-alert>

    <template v-if="!loading && !error && payments.length === 0">
      <n-empty description="No payments found" />
    </template>

    <n-card v-for="payment in payments" :key="payment.paymentId" style="margin-bottom: 16px">
      <template #header>
        <n-space align="center" justify="space-between">
          <n-space align="center">
            <n-tag :type="statusType(payment.status)">{{ payment.status }}</n-tag>
            <n-text depth="3">Payment {{ payment.paymentId.slice(0, 8) }}...</n-text>
          </n-space>
          <n-space>
            <router-link :to="`/payments/${payment.paymentId}`" style="text-decoration: none">
              <n-button size="small">View</n-button>
            </router-link>
            <router-link
              v-if="payment.status === 'COMPLETED'"
              :to="`/payments/${payment.paymentId}/refund`"
              style="text-decoration: none"
            >
              <n-button size="small" type="warning">Refund</n-button>
            </router-link>
          </n-space>
        </n-space>
      </template>

      <n-descriptions :column="2" label-placement="left">
        <n-descriptions-item label="Amount">
          {{ payment.amount?.amount ?? payment.amount }} {{ payment.amount?.currency ?? payment.currency }}
        </n-descriptions-item>
        <n-descriptions-item label="Booking ID">
          <n-text depth="3">{{ payment.bookingId }}</n-text>
        </n-descriptions-item>
        <n-descriptions-item label="Created">
          {{ formatDate(payment.createdAt) }}
        </n-descriptions-item>
        <n-descriptions-item v-if="payment.completedAt" label="Completed">
          {{ formatDate(payment.completedAt) }}
        </n-descriptions-item>
      </n-descriptions>
    </n-card>
  </n-space>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { getPaymentsByUser } from '@/api/paymentApi';

const authStore = useAuthStore();
const loading = ref(true);
const error = ref('');
const payments = ref([]);

const statusType = (status) => {
  const map = { INITIATED: 'info', COMPLETED: 'success', FAILED: 'error' };
  return map[status] || 'default';
};

const formatDate = (dateStr) => {
  if (!dateStr) return 'N/A';
  return new Date(dateStr).toLocaleString('en-GB', {
    day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit'
  });
};

onMounted(async () => {
  try {
    const resp = await getPaymentsByUser(authStore.userId);
    payments.value = resp.data;
  } catch (e) {
    error.value = 'Failed to load payments.';
  } finally {
    loading.value = false;
  }
});
</script>
