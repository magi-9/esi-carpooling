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
    authStore.startRefreshTimer();
}

app.use(router);
app.mount("#app");
