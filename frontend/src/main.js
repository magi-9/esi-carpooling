import { useAuthStore } from "@/stores/auth";
import { createPinia } from "pinia";
import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";

const app = createApp(App);
const pinia = createPinia();

app.use(pinia);

// Re-initialize the refresh timer if a token already exists on load
const authStore = useAuthStore();
if (authStore.token) {
    // It's a good practice to validate the token on startup
    import("@/services/authApi").then(({ default: api }) => {
        api.get("/api/auth/validate")
            .then(() => authStore.startRefreshTimer())
            .catch(() => authStore.clearToken()); // Token was already expired
    });
}

app.use(router);
app.mount("#app");
