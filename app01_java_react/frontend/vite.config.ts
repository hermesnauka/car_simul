import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Dev proxy: the app calls relative /api/... URLs, Vite forwards them to the
// Spring Boot backend, so the browser sees a single origin and CORS never
// enters the picture in development.
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
