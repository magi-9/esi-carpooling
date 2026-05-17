<template>
  <n-space vertical size="large" style="padding: 24px; max-width: 900px; margin: 0 auto">
    <n-h1>My Bookings</n-h1>

    <n-alert v-if="loading" type="info">Loading bookings...</n-alert>
    <n-alert v-if="error" type="error">{{ error }}</n-alert>

    <template v-if="!loading && bookings.length === 0">
      <n-empty description="No bookings found" />
    </template>

    <n-card v-for="booking in bookings" :key="booking.bookingId" style="margin-bottom: 16px">
      <template #header>
        <n-space align="center" justify="space-between">
          <n-space align="center">
            <n-tag :type="statusType(booking.status)">{{ booking.status }}</n-tag>
            <n-text depth="3">Booking {{ booking.bookingId.slice(0, 8) }}...</n-text>
          </n-space>
          <n-popconfirm v-if="booking.status === 'PENDING' || booking.status === 'CONFIRMED'" @positive-click="cancelBooking(booking.bookingId)">
            <template #trigger>
              <n-button size="small" type="error">Cancel</n-button>
            </template>
            Cancel this booking?
          </n-popconfirm>
        </n-space>
      </template>

      <n-space vertical>
        <n-space v-if="rideDetails[booking.rideId]">
          <n-text strong>{{ rideDetails[booking.rideId].startAddress }} → {{ rideDetails[booking.rideId].endAddress }}</n-text>
          <n-text depth="3">{{ formatDate(rideDetails[booking.rideId].rideStartDate) }}</n-text>
        </n-space>

        <template v-if="booking.status === 'COMPLETED'">
          <n-divider style="margin: 12px 0" />

          <!-- Existing review display with edit/delete -->
          <template v-if="existingReviews[booking.bookingId]">
            <template v-if="editReviewId === existingReviews[booking.bookingId].reviewId">
              <n-space vertical>
                <n-rate v-model:value="editReviewForm.stars" :count="5" />
                <n-input v-model:value="editReviewForm.comment" type="textarea" placeholder="Update your review" :rows="2" />
                <n-space>
                  <n-button size="small" type="primary" :loading="savingReview" @click="handleUpdateReview">Save</n-button>
                  <n-button size="small" @click="editReviewId = null">Cancel</n-button>
                </n-space>
                <n-alert v-if="reviewError[booking.bookingId]" type="error" size="small">{{ reviewError[booking.bookingId] }}</n-alert>
              </n-space>
            </template>
            <template v-else>
              <n-alert type="success" size="small">
                <n-space vertical>
                  <n-space justify="space-between" align="center">
                    <n-text strong>
                      {{ '★'.repeat(existingReviews[booking.bookingId].stars) + '☆'.repeat(5 - existingReviews[booking.bookingId].stars) }}
                    </n-text>
                    <n-space>
                      <n-button size="tiny" @click="openEditReview(existingReviews[booking.bookingId])">Edit</n-button>
                      <n-popconfirm @positive-click="handleDeleteReview(existingReviews[booking.bookingId].reviewId)">
                        <template #trigger>
                          <n-button size="tiny" type="error">Delete</n-button>
                        </template>
                        Delete this review?
                      </n-popconfirm>
                    </n-space>
                  </n-space>
                  <n-text>{{ existingReviews[booking.bookingId].comment || 'No comment' }}</n-text>
                </n-space>
              </n-alert>
            </template>
          </template>

          <!-- Review form if no review yet -->
          <n-form v-else @submit.prevent="submitReview(booking.bookingId)" :show-label="false">
            <n-text strong style="margin-bottom: 8px; display: block">Leave a Review</n-text>
            <n-space vertical>
              <n-form-item label="Rating">
                <n-rate v-model:value="reviewForm[booking.bookingId].stars" :count="5" />
              </n-form-item>
              <n-form-item label="Comment">
                <n-input
                  v-model:value="reviewForm[booking.bookingId].comment"
                  type="textarea"
                  placeholder="How was your ride?"
                  :rows="3"
                />
              </n-form-item>
              <n-button type="primary" :loading="submitting[booking.bookingId]" attr-type="submit">
                Submit Review
              </n-button>
              <n-alert v-if="reviewError[booking.bookingId]" type="error" size="small">
                {{ reviewError[booking.bookingId] }}
              </n-alert>
            </n-space>
          </n-form>
        </template>
      </n-space>
    </n-card>
  </n-space>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getReviews, createReview, updateReview, deleteReview } from '@/api/reviewApi';
import { getRide } from '@/api/rideApi';
import { cancelBooking as cancelBookingApi } from '@/api/bookingApi';

const loading = ref(true);
const error = ref('');
const bookings = ref([]);
const rideDetails = ref({});
const existingReviews = ref({});
const reviewForm = ref({});
const submitting = ref({});
const reviewError = ref({});

const editReviewId = ref(null);
const editReviewForm = ref({ stars: 5, comment: '' });
const savingReview = ref(false);

const statusType = (status) => {
  const map = { PENDING: 'warning', CONFIRMED: 'info', COMPLETED: 'success', CANCELLED: 'error' };
  return map[status] || 'default';
};

const formatDate = (dateStr) => {
  if (!dateStr) return 'N/A';
  return new Date(dateStr).toLocaleString('en-GB', {
    day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit'
  });
};

const submitReview = async (bookingId) => {
  reviewError.value[bookingId] = '';
  submitting.value[bookingId] = true;
  const form = reviewForm.value[bookingId];
  if (!form.stars || form.stars < 1 || form.stars > 5) {
    reviewError.value[bookingId] = 'Please select a rating';
    submitting.value[bookingId] = false;
    return;
  }
  try {
    await createReview({ bookingId, stars: form.stars, comment: form.comment || '' });
    await loadReviews();
  } catch (e) {
    reviewError.value[bookingId] = e.response?.data || 'Failed to submit review';
  } finally {
    submitting.value[bookingId] = false;
  }
};

const openEditReview = (review) => {
  editReviewId.value = review.reviewId;
  editReviewForm.value = { stars: review.stars, comment: review.comment || '' };
  reviewError.value = {};
};

const handleUpdateReview = async () => {
  savingReview.value = true;
  try {
    await updateReview(editReviewId.value, editReviewForm.value);
    editReviewId.value = null;
    await loadReviews();
  } catch (e) {
    reviewError.value[editReviewId.value] = e.response?.data || 'Failed to update review';
  } finally {
    savingReview.value = false;
  }
};

const handleDeleteReview = async (reviewId) => {
  try {
    await deleteReview(reviewId);
    await loadReviews();
  } catch (e) {
    error.value = e.response?.data || 'Failed to delete review';
  }
};

const cancelBooking = async (bookingId) => {
  try {
    await cancelBookingApi(bookingId);
    bookings.value = bookings.value.filter(b => b.bookingId !== bookingId);
  } catch (e) {
    error.value = e.response?.data || 'Failed to cancel booking';
  }
};

const loadReviews = async () => {
  try {
    const resp = await getReviews();
    const reviews = resp.data || [];
    const map = {};
    reviews.forEach(r => { map[r.bookingId] = r; });
    existingReviews.value = map;
  } catch (e) {
    console.error('Failed to load reviews:', e);
  }
};

onMounted(async () => {
  try {
    loading.value = true;
    const token = localStorage.getItem('jwt_token');
    const bookingsResp = await fetch(`${import.meta.env.VITE_GATEWAY_API_URL || 'http://localhost:8080'}/api/bookings`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    bookings.value = bookingsResp.ok ? await bookingsResp.json() : [];

    for (const booking of bookings.value) {
      try {
        const rideResp = await getRide(booking.rideId);
        rideDetails.value[booking.rideId] = rideResp.data;
        reviewForm.value[booking.bookingId] = { stars: 5, comment: '' };
      } catch (e) {
        console.error('Failed to load ride:', e);
      }
    }

    await loadReviews();
  } catch (e) {
    error.value = 'Failed to load bookings.';
  } finally {
    loading.value = false;
  }
});
</script>