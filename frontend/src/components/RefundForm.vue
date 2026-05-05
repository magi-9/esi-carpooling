<template>
  <div style="max-width: 400px">
    <h3>Request Refund</h3>
    <div style="margin-bottom: 8px">
      <label>
        Reason
        <span style="color: red">*</span>
      </label>
      <textarea
        v-model="reason"
        rows="4"
        style="width: 100%; padding: 8px; margin-top: 4px; resize: vertical"
        placeholder="Please explain why you are requesting a refund..."
      />
      <span v-if="showError" style="color: red; font-size: 14px">Reason is required.</span>
    </div>
    <button
      @click="submit"
      style="
        width: 100%;
        padding: 10px;
        background: #e74c3c;
        color: white;
        border: none;
        cursor: pointer;
        border-radius: 4px;
        font-size: 16px;
      "
    >
      Submit Refund Request
    </button>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const emit = defineEmits(['submit']);
const reason = ref('');
const showError = ref(false);

function submit() {
  if (!reason.value.trim()) {
    showError.value = true;
    return;
  }
  showError.value = false;
  emit('submit', reason.value.trim());
}
</script>
