import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 端口不固定，由启动方通过 CLI 参数指定（vite 默认转发 --host/--port）
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
