<template>
  <div style="border: 1px solid #ddd; border-radius: 8px; padding: 24px; max-width: 400px">
    <h3>Payment Status</h3>
    <div style="margin-bottom: 12px">
      <span :style="statusStyle">{{ payment.status }}</span>
    </div>
    <table style="width: 100%; border-collapse: collapse; margin-bottom: 16px">
      <tr>
        <td style="padding: 4px 0; color: #666">Payment ID:</td>
        <td style="font-size: 12px">{{ payment.paymentId }}</td>
      </tr>
      <tr>
        <td style="padding: 4px 0; color: #666">Booking:</td>
        <td>{{ payment.bookingId }}</td>
      </tr>
      <tr>
        <td style="padding: 4px 0; color: #666">Amount:</td>
        <td>{{ payment.chargedAmount?.amount }} {{ payment.chargedAmount?.currency }}</td>
      </tr>
      <tr>
        <td style="padding: 4px 0; color: #666">Created:</td>
        <td>{{ formatDate(payment.createdAt) }}</td>
      </tr>
      <tr v-if="payment.completedAt">
        <td style="padding: 4px 0; color: #666">Completed:</td>
        <td>{{ formatDate(payment.completedAt) }}</td>
      </tr>
    </table>

    <button
      v-if="payment.status === 'PROCESSING'"
      @click="$emit('complete')"
      style="
        width: 100%;
        margin-bottom: 8px;
        padding: 10px;
        background: #2980b9;
        color: white;
        border: none;
        cursor: pointer;
        border-radius: 4px;
      "
    >
      Mark as Complete
    </button>
    <button
      v-if="payment.status === 'COMPLETED'"
      @click="$emit('refund')"
      style="
        width: 100%;
        padding: 10px;
        background: #e74c3c;
        color: white;
        border: none;
        cursor: pointer;
        border-radius: 4px;
      "
    >
      Request Refund
    </button>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({ payment: Object });
defineEmits(['complete', 'refund']);

const statusStyle = computed(() => {
  const colors = {
    INITIATED: '#f39c12',
    PROCESSING: '#3498db',
    COMPLETED: '#27ae60',
    REFUNDED: '#8e44ad',
    FAILED: '#e74c3c'
  };
  return {
    fontWeight: 'bold',
    fontSize: '18px',
    color: colors[props.payment?.status] || '#333'
  };
});

function formatDate(iso) {
  return iso ? new Date(iso).toLocaleString() : '—';
}
</script>
