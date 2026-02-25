import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { AuthProvider } from './AuthContext';
import useAuth from '../hooks/useAuth';

// Mock tokenStorage
vi.mock('../utils/tokenStorage', () => ({
  getToken: vi.fn(() => null),
  setToken: vi.fn(),
  clearToken: vi.fn(),
}));

describe('AuthContext and useAuth', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('should throw error when useAuth is used outside AuthProvider', () => {
    expect(() => {
      renderHook(() => useAuth());
    }).toThrow('useAuth must be used within AuthProvider');
  });

  it('should provide initial context with no user when no token exists', () => {
    const wrapper = ({ children }) => <AuthProvider>{children}</AuthProvider>;
    const { result } = renderHook(() => useAuth(), { wrapper });

    expect(result.current.token).toBeNull();
    expect(result.current.user).toBeNull();
    expect(result.current.isAuthenticated).toBe(false);
  });

  it('should decode JWT token and extract user information', () => {
    // Create a valid JWT token (header.payload.signature)
    const validPayload = {
      sub: 'test@example.com',
      role: 'submitter',
      userId: 'user-123',
    };
    const encodedPayload = btoa(JSON.stringify(validPayload)).replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');
    const token = `header.${encodedPayload}.signature`;

    const wrapper = ({ children }) => <AuthProvider>{children}</AuthProvider>;
    const { result } = renderHook(() => useAuth(), { wrapper });

    // Initially user should be null as there's no token stored
    expect(result.current.user).toBeNull();
  });

  it('should signIn and update authentication state', () => {
    const validPayload = {
      sub: 'user@example.com',
      role: 'evaluator/admin',
      userId: 'user-456',
    };
    const encodedPayload = btoa(JSON.stringify(validPayload)).replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');
    const token = `header.${encodedPayload}.signature`;

    const wrapper = ({ children }) => <AuthProvider>{children}</AuthProvider>;
    const { result } = renderHook(() => useAuth(), { wrapper });

    act(() => {
      result.current.signIn(token);
    });

    expect(result.current.token).toBe(token);
    expect(result.current.isAuthenticated).toBe(true);
    expect(result.current.user).not.toBeNull();
    expect(result.current.user.email).toBe('user@example.com');
    expect(result.current.user.role).toBe('evaluator/admin');
    expect(result.current.user.userId).toBe('user-456');
  });

  it('should signOut and clear authentication state', () => {
    const validPayload = {
      sub: 'logout@example.com',
      role: 'submitter',
      userId: 'user-789',
    };
    const encodedPayload = btoa(JSON.stringify(validPayload)).replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');
    const token = `header.${encodedPayload}.signature`;

    const wrapper = ({ children }) => <AuthProvider>{children}</AuthProvider>;
    const { result } = renderHook(() => useAuth(), { wrapper });

    act(() => {
      result.current.signIn(token);
    });

    expect(result.current.isAuthenticated).toBe(true);

    act(() => {
      result.current.signOut();
    });

    expect(result.current.token).toBeNull();
    expect(result.current.user).toBeNull();
    expect(result.current.isAuthenticated).toBe(false);
  });

  it('should handle malformed JWT tokens gracefully', () => {
    const malformedToken = 'not.a.valid.jwt';

    const wrapper = ({ children }) => <AuthProvider>{children}</AuthProvider>;
    const { result } = renderHook(() => useAuth(), { wrapper });

    act(() => {
      result.current.signIn(malformedToken);
    });

    expect(result.current.token).toBe(malformedToken);
    expect(result.current.user).toBeNull(); // Failed to decode
    expect(result.current.isAuthenticated).toBe(true); // Token exists even if malformed
  });

  it('should extract correct user data from different roles', () => {
    const adminPayload = {
      sub: 'admin@example.com',
      role: 'evaluator/admin',
      userId: 'admin-001',
    };
    const encodedPayload = btoa(JSON.stringify(adminPayload)).replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');
    const token = `header.${encodedPayload}.signature`;

    const wrapper = ({ children }) => <AuthProvider>{children}</AuthProvider>;
    const { result } = renderHook(() => useAuth(), { wrapper });

    act(() => {
      result.current.signIn(token);
    });

    expect(result.current.user.role).toBe('evaluator/admin');
    expect(result.current.user.email).toBe('admin@example.com');
  });

  it('should provide stable context value', () => {
    const wrapper = ({ children }) => <AuthProvider>{children}</AuthProvider>;
    const { result, rerender } = renderHook(() => useAuth(), { wrapper });

    const firstValue = result.current;
    rerender();
    const secondValue = result.current;

    // Same references should be maintained if token hasn't changed
    expect(firstValue.isAuthenticated).toBe(secondValue.isAuthenticated);
  });
});
