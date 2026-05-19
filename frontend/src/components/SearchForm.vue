<template>
  <form
    @submit.prevent="submit"
    style="display: flex; flex-direction: column; gap: 12px; max-width: 500px"
  >
    <div style="display: flex; gap: 12px">
      <div style="flex: 1">
        <label>Origin</label>
        <input
          v-model="form.originAddress"
          placeholder="e.g. Tallinn"
          required
          style="width: 100%; padding: 8px; margin-top: 4px"
        />
      </div>
      <div style="flex: 1">
        <label>Destination</label>
        <input
          v-model="form.destinationAddress"
          placeholder="e.g. Tartu"
          required
          style="width: 100%; padding: 8px; margin-top: 4px"
        />
      </div>
    </div>
    <div style="display: flex; gap: 12px">
      <div style="flex: 1">
        <label>Departure Date</label>
        <input
          v-model="form.departureDate"
          type="date"
          required
          style="width: 100%; padding: 8px; margin-top: 4px"
        />
      </div>
      <div style="flex: 1">
        <label>Max Price/Seat (€)</label>
        <input
          v-model.number="form.maxPricePerSeat"
          type="number"
          min="0"
          placeholder="Any"
          style="width: 100%; padding: 8px; margin-top: 4px"
        />
      </div>
    </div>
    <button
      type="submit"
      style="
        padding: 10px;
        background: #2c3e50;
        color: white;
        border: none;
        cursor: pointer;
        font-size: 16px;
      "
    >
      Search Rides
    </button>
  </form>
</template>

<script setup>
import { reactive } from 'vue';

const emit = defineEmits(['search']);

const form = reactive({
  originAddress: '',
  destinationAddress: '',
  departureDate: '',
  maxPricePerSeat: null
});

function submit() {
  emit('search', {
    originLat: null,
    originLon: null,
    originAddress: form.originAddress,
    destinationLat: null,
    destinationLon: null,
    destinationAddress: form.destinationAddress,
    departureDate: form.departureDate,
    seatsNeeded: 1,
    maxPricePerSeat: form.maxPricePerSeat || undefined
  });
}
</script>
