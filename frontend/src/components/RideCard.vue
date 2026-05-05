<template>
  <div
    style="
      border: 1px solid #ddd;
      border-radius: 8px;
      padding: 16px;
      margin-bottom: 12px;
      background: white;
    "
  >
    <div style="display: flex; justify-content: space-between; align-items: flex-start">
      <div>
        <div style="font-weight: bold; font-size: 16px">Ride {{ recommendation.rideId }}</div>
        <div style="color: #666; margin-top: 4px">
          <span>{{ starsDisplay }}</span>
          <span style="margin-left: 8px">{{ recommendation.driverRating?.toFixed(1) }}/5</span>
        </div>
        <div style="margin-top: 8px; color: #555">
          Distance to pickup:
          <strong>{{ recommendation.distanceToOriginKm?.toFixed(1) }} km</strong>
        </div>
        <div style="color: #555">
          Distance to dropoff:
          <strong>{{ recommendation.distanceToDestinationKm?.toFixed(1) }} km</strong>
        </div>
      </div>
      <div style="text-align: right">
        <div style="font-size: 20px; font-weight: bold; color: #27ae60">
          Relevance: {{ (recommendation.relevanceScore * 100).toFixed(0) }}%
        </div>
        <button
          @click="$emit('book', recommendation.rideId)"
          style="
            margin-top: 12px;
            padding: 8px 16px;
            background: #27ae60;
            color: white;
            border: none;
            cursor: pointer;
            border-radius: 4px;
          "
        >
          Book &amp; Pay
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({ recommendation: Object });
defineEmits(['book']);

const starsDisplay = computed(() => {
  const rating = props.recommendation?.driverRating || 0;
  const full = Math.round(rating);
  return '★'.repeat(full) + '☆'.repeat(5 - full);
});
</script>
