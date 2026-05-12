<template>
  <div style="padding: 24px; max-width: 900px; margin: 0 auto">
    <h1>Payment</h1>

    <div v-if="loading" style="color: #666">Loading...</div>
    <div v-if="error" style="color: red; margin-bottom: 16px">{{ error }}</div>

    <!-- Initiate new payment -->
    <div v-if="isNew && !payment">
      <div v-if="missingNewDetails" style="color: red; margin-bottom: 16px">
        {{ missingNewDetails }}. Create the payment from a successful booking.
      </div>
      <PaymentConfirmation
        v-if="!missingNewDetails"
        :booking-id="bookingId"
        :amount="amount"
        :currency="currency"
        :payer-id="payerId"
        :payee-id="payeeId"
        @confirm="handleConfirm"
      />
    </div>

    <!-- Show existing payment status -->
    <div v-if="payment">
      <PaymentStatus :payment="payment" @complete="handleComplete" @refund="handleRefund" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import PaymentConfirmation from '../components/PaymentConfirmation.vue';
import PaymentStatus from '../components/PaymentStatus.vue';
import { initiatePayment, getPayment, completePayment } from '../api/paymentApi';
import { useAuthStore } from '../stores/auth';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const payment = ref(null);
const loading = ref(false);
const error = ref('');

const isNew = computed(() => {
  if (route.name === 'NewPayment') {
    return true;
  }

  const paymentId = route.params.paymentId;
  if (!paymentId || paymentId === 'new') {
    return true;
  }

  return false;
});
const bookingId = computed(() => String(route.query.bookingId || '').trim());
const amount = computed(() => {
  const raw = route.query.amount;
  if (raw === undefined || raw === null || raw === '') {
    return null;
  }
  const parsed = Number(raw);
  return Number.isFinite(parsed) ? parsed : null;
});
const currency = computed(() => String(route.query.currency || 'EUR').trim() || 'EUR');
const payeeId = computed(() => String(route.query.payeeId || '').trim());
const payerId = computed(() => authStore.userId || 'current-user');
const missingNewDetails = computed(() => {
  if (!isNew.value) {
    return '';
  }

  const missing = [];
  if (!bookingId.value) missing.push('bookingId');
  if (!payeeId.value) missing.push('payeeId');
  if (amount.value === null) missing.push('amount');

  return missing.length ? `Missing payment details: ${missing.join(', ')}` : '';
});

onMounted(async () => {
  if (isNew.value) {
    return;
  }

  const paymentId = route.params.paymentId;
  if (!paymentId || paymentId === 'new') {
    error.value = 'Payment not found';
    return;
  }

  loading.value = true;
  try {
    const res = await getPayment(paymentId);
    payment.value = res.data;
  } catch (e) {
    error.value = e.response?.data?.error || 'Payment not found';
  } finally {
    loading.value = false;
  }
});

async function handleConfirm() {
  if (missingNewDetails.value) {
    error.value = missingNewDetails.value;
    return;
  }

  loading.value = true;
  error.value = '';
  try {
    const res = await initiatePayment({
      bookingId: bookingId.value,
      payerId: payerId.value,
      payeeId: payeeId.value,
      amount: amount.value,
      currency: currency.value
    });
    payment.value = res.data;
    router.replace(`/payments/${res.data.paymentId}`);
  } catch (e) {
    error.value = e.response?.data?.error || 'Failed to initiate payment';
  } finally {
    loading.value = false;
  }
}

async function handleComplete() {
  loading.value = true;
  try {
    const res = await completePayment(payment.value.paymentId);
    payment.value = res.data;
  } catch (e) {
    error.value = e.response?.data?.error || 'Failed to complete payment';
  } finally {
    loading.value = false;
  }
}

function handleRefund() {
  router.push(`/payments/${payment.value.paymentId}/refund`);
}
</script>
