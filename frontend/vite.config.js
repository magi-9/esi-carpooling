import vue from "@vitejs/plugin-vue";
import { fileURLToPath, URL } from "node:url";
import { defineConfig } from "vite";

export default defineConfig({
    plugins: [vue()],
    server: { port: 3000 },
    resolve: {
        alias: {
            // This tells Vite that '@' points to the 'src' directory
            "@": fileURLToPath(new URL("./src", import.meta.url)),
        },
    },
});
