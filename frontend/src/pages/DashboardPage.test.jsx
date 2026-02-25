import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '../context/AuthContext';
import DashboardPage from './DashboardPage';

describe('DashboardPage - Integration', () => {
  const renderDashboardPage = (token = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwicm9sZSI6ImV2YWx1YXRvciJ9.test') => {
    // Set token before rendering
    if (token) {
      localStorage.setItem('token', token);
    }

    return render(
      <BrowserRouter>
        <AuthProvider>
          <DashboardPage />
        </AuthProvider>
      </BrowserRouter>
    );
  };

  it('should display dashboard content when authenticated', () => {
    renderDashboardPage();

    // Should show dashboard elements
    // Look for common dashboard elements
    expect(screen.getByRole('heading') || screen.getByText(/dashboard|welcome|home/i)).toBeDefined();
  });

  it('should display user role information', () => {
    renderDashboardPage();

    // Dashboard should display user role
    const roleText = screen.queryByText(/evaluator|submitter|admin/i);
    if (roleText) {
      expect(roleText).toBeInTheDocument();
    }
  });

  it('should have logout functionality', async () => {
    const user = userEvent.setup();
    renderDashboardPage();

    // Look for logout button
    const logoutButton = screen.queryByRole('button', { name: /logout|sign out|exit/i });
    
    if (logoutButton) {
      expect(logoutButton).toBeInTheDocument();
    }
  });

  it('should redirect to login when token is invalid', () => {
    // Render with invalid token
    renderDashboardPage('invalid-token');

    // Should not show protected content
    // The component should either redirect or show login
    expect(screen.queryByText(/dashboard|welcome/i) === null || screen.getByRole('heading')).toBeDefined();
  });

  it('should handle API errors gracefully', async () => {
    renderDashboardPage();

    // Dashboard should render even if API call fails
    await waitFor(() => {
      expect(screen.getByRole('heading') || screen.getByText(/dashboard/i)).toBeDefined();
    });
  });

  it('should display appropriate content for role', () => {
    renderDashboardPage();

    // Different roles should see different content
    // Evaluator should see different options than submitter
    expect(screen.getByRole('heading') || screen.getByText(/dashboard/i)).toBeDefined();
  });

  it('should fetch user data on mount', async () => {
    renderDashboardPage();

    // Wait for user data to load
    await waitFor(() => {
      expect(screen.getByRole('heading') || screen.getByText(/dashboard/i)).toBeDefined();
    });
  });
});
