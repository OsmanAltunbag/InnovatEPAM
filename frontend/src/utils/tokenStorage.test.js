import { describe, it, expect, beforeEach, vi } from 'vitest';
import { getToken, setToken, clearToken } from './tokenStorage';

describe('tokenStorage', () => {
  const TOKEN_KEY = 'innovatepam.jwt';
  const mockToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token';

  beforeEach(() => {
    // Clear localStorage before each test
    localStorage.clear();
    vi.clearAllMocks();
  });

  describe('setToken', () => {
    it('should store token in localStorage', () => {
      setToken(mockToken);
      expect(localStorage.getItem(TOKEN_KEY)).toBe(mockToken);
    });

    it('should overwrite existing token', () => {
      setToken('old-token');
      setToken(mockToken);
      expect(localStorage.getItem(TOKEN_KEY)).toBe(mockToken);
    });

    it('should handle empty string', () => {
      setToken('');
      expect(localStorage.getItem(TOKEN_KEY)).toBe('');
    });
  });

  describe('getToken', () => {
    it('should return token from localStorage', () => {
      localStorage.setItem(TOKEN_KEY, mockToken);
      expect(getToken()).toBe(mockToken);
    });

    it('should return null when no token exists', () => {
      expect(getToken()).toBeNull();
    });

    it('should return empty string if empty token was stored', () => {
      localStorage.setItem(TOKEN_KEY, '');
      expect(getToken()).toBe('');
    });
  });

  describe('clearToken', () => {
    it('should remove token from localStorage', () => {
      localStorage.setItem(TOKEN_KEY, mockToken);
      clearToken();
      expect(localStorage.getItem(TOKEN_KEY)).toBeNull();
    });

    it('should not throw error when no token exists', () => {
      expect(() => clearToken()).not.toThrow();
      expect(localStorage.getItem(TOKEN_KEY)).toBeNull();
    });

    it('should leave other localStorage items untouched', () => {
      localStorage.setItem(TOKEN_KEY, mockToken);
      localStorage.setItem('other-key', 'other-value');
      clearToken();
      expect(localStorage.getItem(TOKEN_KEY)).toBeNull();
      expect(localStorage.getItem('other-key')).toBe('other-value');
    });
  });
});
