import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // Proxy API calls to backend during dev so we don't hit CORS
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/v3/api-docs': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
    },
  },
})
