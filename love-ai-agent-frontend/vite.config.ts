import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      // 将前端的 /api 开头的请求代理到后端 http://localhost:8123
      // 例如 /api/ai/xxx -> http://localhost:8123/api/ai/xxx
      '/api': {
        target: 'http://localhost:8123',
        changeOrigin: true,
        // 这里不做 rewrite，保持路径中携带 /api 前缀，由后端处理
      },
    },
  },
});


