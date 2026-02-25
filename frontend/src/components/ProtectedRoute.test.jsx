import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import AuthContext from '../context/AuthContext';
import ProtectedRoute from './ProtectedRoute';

// Mock component for testing
const ProtectedComponent = () => <div>Protected Content</div>;

describe('ProtectedRoute', () => {
  beforeEach(() => {
    // Clear any existing localStorage data
    localStorage.clear();
  });

  it('should render protected component when user is authenticated', () => {
    // Create a mock auth context with authenticated user
    const mockAuthContext = {
      token: 'mock-token',
      user: { email: 'test@example.com', role: 'submitter' },
      signIn: vi.fn(),
      signOut: vi.fn(),
      isAuthenticated: true
    };

    render(
      <BrowserRouter>
        <AuthContext.Provider value={mockAuthContext}>
          <ProtectedRoute>
            <ProtectedComponent />
          </ProtectedRoute>
        </AuthContext.Provider>
      </BrowserRouter>
    );

    expect(screen.getByText('Protected Content')).toBeInTheDocument();
  });

  it('should render access denied message when user is not authenticated', () => {
    // Create a mock auth context with no user
    const mockAuthContext = {
      token: null,
      user: null,
      signIn: vi.fn(),
      signOut: vi.fn(),
      isAuthenticated: false
    };

    render(
      <BrowserRouter>
        <AuthContext.Provider value={mockAuthContext}>
          <ProtectedRoute>
            <ProtectedComponent />
          </ProtectedRoute>
        </AuthContext.Provider>
      </BrowserRouter>
    );

    // Should not show protected content
    expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
    expect(screen.getByText(/access denied/i)).toBeInTheDocument();
    expect(screen.getByText(/please log in/i)).toBeInTheDocument();
  });

  it('should handle missing token gracefully', () => {
    // Create a mock auth context with no authentication
    const mockAuthContext = {
      token: null,
      user: null,
      signIn: vi.fn(),
      signOut: vi.fn(),
      isAuthenticated: false
    };

    render(
      <BrowserRouter>
        <AuthContext.Provider value={mockAuthContext}>
          <ProtectedRoute>
            <ProtectedComponent />
          </ProtectedRoute>
        </AuthContext.Provider>
      </BrowserRouter>
    );

    // Should handle missing token without errors
    expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
    expect(screen.getByText(/access denied/i)).toBeInTheDocument();
  });
});
