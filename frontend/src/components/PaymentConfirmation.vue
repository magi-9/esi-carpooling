<template>
  <div style="border: 1px solid #ddd; border-radius: 8px; padding: 24px; max-width: 400px">
    <h3>Confirm Payment</h3>
    <table style="width: 100%; border-collapse: collapse; margin-bottom: 16px">
      <tr>
        <td style="padding: 6px 0; color: #666">Booking ID:</td>
        <td>
          <strong>{{ bookingId }}</strong>
        </td>
      </tr>
      <tr>
        <td style="padding: 6px 0; color: #666">Amount:</td>
        <td>
          <strong>{{ amount }} {{ currency }}</strong>
        </td>
      </tr>
      <tr>
        <td style="padding: 6px 0; color: #666">Payer:</td>
        <td>{{ payerId }}</td>
      </tr>
      <tr>
        <td style="padding: 6px 0; color: #666">Payee:</td>
        <td>{{ payeeId }}</td>
      </tr>
    </table>
    <button
      @click="$emit('confirm')"
      style="
        width: 100%;
        padding: 12px;
        background: #27ae60;
        color: white;
        border: none;
        cursor: pointer;
        font-size: 16px;
        border-radius: 4px;
      "
    >
      Confirm &amp; Pay
    </button>
  </div>
</template>

<script setup>
defineProps({
  bookingId: String,
  amount: Number,
  currency: { type: String, default: 'EUR' },
  payerId: String,
  payeeId: String
});
defineEmits(['confirm']);
</script>
