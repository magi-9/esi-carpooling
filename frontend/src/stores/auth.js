import authApi from "@/services/authApi";
import { defineStore } from "pinia";

export const useAuthStore = defineStore("auth", {
    state: () => ({
        token: localStorage.getItem("jwt_token") || null,
        refreshTimer: null,
    }),

    getters: {
        isAuthenticated: (state) => !!state.token,
    },

    actions: {
        // --------------------------------------------------
        // Core Authentication Flow
        // --------------------------------------------------
        async login(email, password) {
            try {
                const response = await authApi.post("/api/auth/login", {
                    email,
                    password,
                });
                this.setToken(response.data.token); // Adjust based on exact backend JSON key
                return true;
            } catch (error) {
                console.error("Login failed:", error);
                throw error;
            }
        },

        async register(email, password, roles) {
            try {
                // Roles must be an array containing "PASSENGER", "DRIVER", or both
                const response = await authApi.post("/api/auth/register", {
                    email,
                    password,
                    roles,
                });
                this.setToken(response.data.token);
                return true;
            } catch (error) {
                console.error("Registration failed:", error);
                throw error;
            }
        },

        async logout() {
            try {
                // Clear security context on backend
                await authApi.post("/api/auth/logout");
            } catch (error) {
                console.error("Backend logout failed, forcing local logout.");
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
            localStorage.setItem("jwt_token", newToken);
            this.startRefreshTimer();
        },

        clearToken() {
            this.token = null;
            localStorage.removeItem("jwt_token");
            this.stopRefreshTimer();
        },

        async refreshToken() {
            try {
                // The spec requires an existing valid token in the header
                const response = await authApi.post("/api/auth/refresh");
                this.setToken(response.data.token);
            } catch (error) {
                console.error("Token refresh failed. Logging out.");
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
            const response = await authApi.get("/api/auth/roles");
            return response.data;
        },

        async checkRole(role) {
            try {
                await authApi.get(`/api/auth/validate/role/${role}`);
                return true;
            } catch (error) {
                return false;
            }
        },
    },
});
