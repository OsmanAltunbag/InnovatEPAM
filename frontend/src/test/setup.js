import '@testing-library/jest-dom';
import { cleanup } from '@testing-library/react';
import { afterEach, beforeAll, afterAll, beforeEach } from 'vitest';
import { server } from './mocks/server';
import React from 'react';

// React'i global yap
window.React = React;

// Global JWT token for protected routes
const GLOBAL_MOCK_TOKEN = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwicm9sZSI6ImV2YWx1YXRvciJ9.test';

// Testlerden önce MSW Mock sunucusunu başlat
beforeAll(() => {
  server.listen({ onUnhandledRequest: 'bypass' });
});

// Her test öncesi localStorage'a token ekle (test tarafından override edilebilir)
beforeEach(() => {
  localStorage.clear();
  localStorage.setItem('innovatepam.jwt', GLOBAL_MOCK_TOKEN);
});

// Her testten sonra DOM'u ve Mock'ları temizle
afterEach(() => {
  cleanup();
  server.resetHandlers();
  localStorage.clear();
});

// Tüm testler bitince sunucuyu kapat
afterAll(() => server.close());