import { describe, it, expect, beforeEach } from 'vitest';
import api from './api';
import { clearToken, setToken } from '../utils/tokenStorage';

describe('api client', () => {
  beforeEach(() => {
    clearToken();
  });

  it('should use default base URL when env is not set', () => {
    expect(api.defaults.baseURL).toBe('http://localhost:8080/api/v1');
  });

  it('should attach Authorization header when token exists', () => {
    setToken('test-token');

    const handler = api.interceptors.request.handlers[0];
    const config = handler.fulfilled({ headers: {} });

    expect(config.headers.Authorization).toBe('Bearer test-token');
  });

  it('should not attach Authorization header when token is missing', () => {
    const handler = api.interceptors.request.handlers[0];
    const config = handler.fulfilled({ headers: {} });

    expect(config.headers.Authorization).toBeUndefined();
  });
});
