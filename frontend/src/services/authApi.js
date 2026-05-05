import { useAuthStore } from "@/stores/auth";
import axios from "axios";

// The base URL comes from your OpenAPI spec's servers list
const authApi = axios.create({
    baseURL: "http://localhost:8086",
    headers: {
        "Content-Type": "application/json",
    },
});

// Request Interceptor: Attach the token to every request
authApi.interceptors.request.use(
    (config) => {
        const authStore = useAuthStore();
        if (authStore.token) {
            config.headers.Authorization = `Bearer ${authStore.token}`;
        }
        return config;
    },
    (error) => Promise.reject(error),
);

// Response Interceptor: Handle expired tokens or unauthorized access globally
authApi.interceptors.response.use(
    (response) => response,
    async (error) => {
        const authStore = useAuthStore();

        if (error.response && error.response.status === 401) {
            // If the token is missing or expired, clear it locally
            // Don't call logout() as the token is already invalid
            authStore.clearToken();
            // Optional: Redirect to login page using Vue Router
            // router.push('/login');
        }
        return Promise.reject(error);
    },
);

export default authApi;
