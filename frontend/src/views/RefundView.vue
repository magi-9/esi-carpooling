<template>
  <div style="padding:24px;max-width:900px;margin:0 auto">
    <h1>Request Refund</h1>
    <p style="color:#666">Payment ID: {{ paymentId }}</p>

    <div v-if="!submitted">
      <div v-if="error" style="color:red;margin-bottom:16px">{{ error }}</div>
      <RefundForm @submit="handleSubmit" />
    </div>

    <div v-else style="border:1px solid #27ae60;border-radius:8px;padding:24px;max-width:400px">
      <h3 style="color:#27ae60">Refund Submitted</h3>
      <table style="width:100%;border-collapse:collapse">
        <tr><td style="padding:4px 0;color:#666">Refund ID:</td><td style="font-size:12px">{{ refund.refundId }}</td></tr>
        <tr><td style="padding:4px 0;color:#666">Amount:</td><td>{{ refund.refundedAmount?.amount }} {{ refund.refundedAmount?.currency }}</td></tr>
        <tr><td style="padding:4px 0;color:#666">Reason:</td><td>{{ refund.reason }}</td></tr>
        <tr><td style="padding:4px 0;color:#666">Processed:</td><td>{{ new Date(refund.processedAt).toLocaleString() }}</td></tr>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import RefundForm from '../components/RefundForm.vue'
import { requestRefund } from '../api/paymentApi'

const route = useRoute()
const paymentId = route.params.paymentId
const submitted = ref(false)
const refund = ref(null)
const error = ref('')

async function handleSubmit(reason) {
  error.value = ''
  try {
    const res = await requestRefund(paymentId, { reason })
    refund.value = res.data
    submitted.value = true
  } catch (e) {
    error.value = e.response?.data?.error || 'Failed to submit refund request'
  }
}
</script>
