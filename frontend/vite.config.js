import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: './src/test/setup.js',
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html', 'json', 'lcov'],
      exclude: [
        'node_modules/',
        'src/test/',
        'src/main.jsx',
        'src/App.jsx',
        '**/*.config.*',
        '**/**.d.ts',
        '**/mockData.js'
      ],
      lines: 80,
      branches: 75,
      functions: 80,
      statements: 80,
      all: true
    }
  }
})