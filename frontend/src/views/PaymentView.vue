<template>
  <div style="padding: 24px; max-width: 900px; margin: 0 auto">
    <h1>Payment</h1>

    <div v-if="loading" style="color: #666">Loading...</div>
    <div v-if="error" style="color: red; margin-bottom: 16px">{{ error }}</div>

    <!-- Initiate new payment -->
    <div v-if="isNew && !payment">
      <PaymentConfirmation
        :booking-id="bookingId"
        :amount="25"
        currency="EUR"
        payer-id="current-user"
        payee-id="driver"
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

const route = useRoute();
const router = useRouter();
const payment = ref(null);
const loading = ref(false);
const error = ref('');

const isNew = computed(() => route.params.paymentId === 'new');
const bookingId = computed(() => route.query.bookingId || 'unknown-booking');

onMounted(async () => {
  if (!isNew.value) {
    loading.value = true;
    try {
      const res = await getPayment(route.params.paymentId);
      payment.value = res.data;
    } catch (e) {
      error.value = e.response?.data?.error || 'Payment not found';
    } finally {
      loading.value = false;
    }
  }
});

async function handleConfirm() {
  loading.value = true;
  error.value = '';
  try {
    const res = await initiatePayment({
      bookingId: bookingId.value,
      payerId: 'current-user',
      payeeId: 'driver',
      amount: 25.0,
      currency: 'EUR'
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
