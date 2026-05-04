import { defineConfig } from "vite";
import { svelte } from "@sveltejs/vite-plugin-svelte";
import path from "path";

export default defineConfig({
  plugins: [svelte()],
  build: {
    outDir: path.resolve(__dirname, "../../../target/classes/static"),
    emptyOutDir: true,
  },
  server: {
    port: 5173,
    proxy: {
      "/api": "http://localhost:8080",
    },
  },
  resolve: {
    alias: {
      $lib: path.resolve(__dirname, "src/lib"),
    },
  },
});
