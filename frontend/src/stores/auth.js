import authApi from '@/services/authApi';
import { defineStore } from 'pinia';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('jwt_token') || null,
    userId: localStorage.getItem('user_id') || null,
    refreshTimer: null
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    currentUserId: (state) => state.userId
  },

  actions: {
    // Helper to safely decode JWT payload
    decodeToken(token) {
      try {
        // JWT format: header.payload.signature
        const parts = token.split('.');
        if (parts.length !== 3) {
          throw new Error('Invalid token format: expected 3 parts');
        }

        const payload = JSON.parse(atob(parts[1]));
        return payload;
      } catch (error) {
        console.error('Failed to decode JWT:', error.message);
        return null;
      }
    },

    // Extract userId from decoded JWT token
    decodeUserId(token) {
      const payload = this.decodeToken(token);
      if (!payload) {
        return null;
      }

      // The 'sub' (subject) claim contains the user ID
      const userId = payload.sub;

      if (!userId) {
        console.warn("Warning: Token does not contain 'sub' claim with userId");
        return null;
      }

      return userId;
    },

    // Get email from token
    getEmailFromToken(token) {
      const payload = this.decodeToken(token);
      return payload?.email || null;
    },

    // Get roles from token
    getRolesFromToken(token) {
      const payload = this.decodeToken(token);
      return payload?.roles || [];
    },

    // Check if token is expired
    isTokenExpired(token) {
      const payload = this.decodeToken(token);
      if (!payload || !payload.exp) {
        return true;
      }

      // exp is in seconds, compare with current time in seconds
      const now = Math.floor(Date.now() / 1000);
      return payload.exp <= now;
    },

    // --------------------------------------------------
    // Core Authentication Flow
    // --------------------------------------------------
    async login(email, password) {
      try {
        const response = await authApi.post('/api/auth/login', {
          email,
          password
        });
        this.setToken(response.data.token); // Adjust based on exact backend JSON key
        return true;
      } catch (error) {
        console.error('Login failed:', error);
        throw error;
      }
    },

    async register(email, password, roles) {
      try {
        // Roles must be an array containing "PASSENGER", "DRIVER", or both
        const response = await authApi.post('/api/auth/register', {
          email,
          password,
          roles
        });
        this.setToken(response.data.token);
        return true;
      } catch (error) {
        console.error('Registration failed:', error);
        throw error;
      }
    },

    async logout() {
      try {
        // Clear security context on backend
        await authApi.post('/api/auth/logout');
      } catch (error) {
        console.error('Backend logout failed, forcing local logout.', error);
      } finally {
        // The client must delete the token locally to complete logout
        this.clearToken();
      }
    },

    // --------------------------------------------------
    // Token Management & Refresh
    // --------------------------------------------------
    setToken(newToken) {
      this.token = newToken;
      localStorage.setItem('jwt_token', newToken);
      this.userId = this.decodeUserId(newToken);
      localStorage.setItem('user_id', this.userId);
      this.startRefreshTimer();
    },

    clearToken() {
      this.token = null;
      this.userId = null;
      localStorage.removeItem('jwt_token');
      localStorage.removeItem('user_id');
      this.stopRefreshTimer();
    },

    async refreshToken() {
      try {
        // The spec requires an existing valid token in the header
        const response = await authApi.post('/api/auth/refresh');
        this.setToken(response.data.token);
      } catch (error) {
        console.error('Token refresh failed. Logging out.', error);
        this.clearToken();
      }
    },

    // Proactively refresh the token before the 15-minute expiration
    startRefreshTimer() {
      this.stopRefreshTimer();
      // 14 minutes in milliseconds (14 * 60 * 1000 = 840000)
      const refreshInterval = 840000;
      this.refreshTimer = setTimeout(() => {
        this.refreshToken();
      }, refreshInterval);
    },

    stopRefreshTimer() {
      if (this.refreshTimer) {
        clearTimeout(this.refreshTimer);
        this.refreshTimer = null;
      }
    },

    // --------------------------------------------------
    // Role Management (Optional Utilities)
    // --------------------------------------------------
    async fetchRoles() {
      const response = await authApi.get('/api/auth/roles');
      return response.data;
    },

    async checkRole(role) {
      try {
        await authApi.get(`/api/auth/validate/role/${role}`);
        return true;
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
      } catch (error) {
        return false;
      }
    }
  }
});
