import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'node:path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5175,
    open: false,
    host: true
  },
  optimizeDeps: {
    include: ['@element-plus/icons-vue']
  }
})
