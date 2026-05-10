import vue from "@vitejs/plugin-vue";
import { fileURLToPath, URL } from "node:url";
import { defineConfig } from "vite";

export default defineConfig({
    plugins: [vue()],
    server: {
        host: "0.0.0.0",
        port: 3000,
        watch: {
            // use polling inside containers to avoid missed events
            usePolling: true,
        },
        hmr: {
            protocol: "ws",
            // client will connect to the host where the browser is running
            // on Linux hosts this can be left default; change to 'host.docker.internal' on mac/windows if needed
        },
    },
    resolve: {
        alias: {
            // This tells Vite that '@' points to the 'src' directory
            "@": fileURLToPath(new URL("./src", import.meta.url)),
        },
    },
});
