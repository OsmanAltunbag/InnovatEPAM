import { describe, it, expect, vi } from 'vitest';
import { renderHook } from '@testing-library/react';
import useAuth from './useAuth';
import AuthContext from '../context/AuthContext';
import React from 'react';

describe('useAuth hook', () => {
  it('should throw error when used outside AuthProvider', () => {
    expect(() => {
      renderHook(() => useAuth());
    }).toThrow('useAuth must be used within AuthProvider');
  });

  it('should return auth context when used within AuthProvider', () => {
    const mockContextValue = {
      token: 'test-token',
      user: { email: 'test@example.com', role: 'submitter' },
      signIn: vi.fn(),
      signOut: vi.fn(),
      isAuthenticated: true,
    };

    const wrapper = ({ children }) => 
      React.createElement(
        AuthContext.Provider,
        { value: mockContextValue },
        children
      );

    const { result } = renderHook(() => useAuth(), { wrapper });

    expect(result.current).toBe(mockContextValue);
    expect(result.current.token).toBe('test-token');
    expect(result.current.isAuthenticated).toBe(true);
  });

  it('should provide access to signIn and signOut methods', () => {
    const mockSignIn = vi.fn();
    const mockSignOut = vi.fn();
    const mockContextValue = {
      token: null,
      user: null,
      signIn: mockSignIn,
      signOut: mockSignOut,
      isAuthenticated: false,
    };

    const wrapper = ({ children }) => 
      React.createElement(
        AuthContext.Provider,
        { value: mockContextValue },
        children
      );

    const { result } = renderHook(() => useAuth(), { wrapper });

    expect(result.current.signIn).toBeDefined();
    expect(result.current.signOut).toBeDefined();
    expect(typeof result.current.signIn).toBe('function');
    expect(typeof result.current.signOut).toBe('function');
  });
});
