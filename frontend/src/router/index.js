import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '../stores/auth'; // Import the Pinia store

import HomeView from '../views/HomeView.vue';
import PaymentView from '../views/PaymentView.vue';
import ProfileView from '../views/ProfileView.vue';
import RefundView from '../views/RefundView.vue';
import SearchView from '../views/SearchView.vue';
import CreateRideView from '../views/CreateRideView.vue';
import MyRidesView from '../views/MyRidesView.vue';
import MyBookingsView from '../views/MyBookingsView.vue';

import LoginForm from '../components/LoginForm.vue';
import RegisterForm from '../components/RegisterForm.vue';

const routes = [
  {
    path: '/',
    name: 'Home',
    component: HomeView
  },
  // --- Authentication Routes ---
  {
    path: '/login',
    name: 'Login',
    component: LoginForm,
    meta: { requiresGuest: true } // Only visible to logged-out users
  },
  {
    path: '/register',
    name: 'Register',
    component: RegisterForm,
    meta: { requiresGuest: true }
  },
  // --- Protected Routes ---
  {
    path: '/search',
    name: 'Search',
    component: SearchView,
    meta: { requiresAuth: true } // Requires a valid JWT
  },
  {
    path: '/rides/create',
    name: 'CreateRide',
    component: CreateRideView,
    meta: { requiresAuth: true, requiresDriver: true }
  },
  {
    path: '/my-rides',
    name: 'MyRides',
    component: MyRidesView,
    meta: { requiresAuth: true, requiresDriver: true }
  },
  {
    path: '/bookings',
    name: 'Bookings',
    component: MyBookingsView,
    meta: { requiresAuth: true }
  },
  {
    path: '/payments/new',
    name: 'NewPayment',
    component: PaymentView,
    meta: { requiresAuth: true }
  },
  {
    path: '/payments/:paymentId',
    name: 'PaymentDetails',
    component: PaymentView,
    meta: { requiresAuth: true }
  },
  {
    path: '/payments/:paymentId/refund',
    name: 'RefundPayment',
    component: RefundView,
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: ProfileView,
    meta: { requiresAuth: true }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// --- Global Navigation Guard ---
router.beforeEach((to, from, next) => {
  // Always call useAuthStore inside the guard to ensure Pinia is initialized
  const authStore = useAuthStore();
  const isAuthenticated = authStore.isAuthenticated;

  // 1. User is trying to access a protected route but is not logged in
  if (to.meta.requiresAuth && !isAuthenticated) {
    next({ name: 'Login' });
  }
  // 2. User is already logged in but tries to access Login or Register
  else if (to.meta.requiresGuest && isAuthenticated) {
    next({ name: 'Search' }); // Redirect them to their main view
  }
  // 3. Check DRIVER role for routes that require it
  else if (to.meta.requiresDriver && isAuthenticated) {
    const roles = authStore.getRolesFromToken(authStore.token);
    if (!roles.includes('DRIVER')) {
      next({ name: 'Search' }); // Redirect non-drivers to search
    } else {
      next();
    }
  }
  // 4. User is allowed to proceed normally
  else {
    next();
  }
});

export default router;
