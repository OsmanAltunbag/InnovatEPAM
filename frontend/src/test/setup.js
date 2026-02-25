import '@testing-library/jest-dom';
import { cleanup } from '@testing-library/react';
import { afterEach, beforeAll, afterAll } from 'vitest';
import { server } from './mocks/server';
import React from 'react';

// React'i global yap
window.React = React;

// Testlerden önce MSW Mock sunucusunu başlat
beforeAll(() => server.listen({ onUnhandledRequest: 'bypass' }));

// Her testten sonra DOM'u ve Mock'ları temizle
afterEach(() => {
  cleanup();
  server.resetHandlers();
});

// Tüm testler bitince sunucuyu kapat
afterAll(() => server.close());