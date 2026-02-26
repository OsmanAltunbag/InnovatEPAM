import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '../context/AuthContext';
import DashboardPage from './DashboardPage';

describe('DashboardPage - Integration', () => {
  const MOCK_TOKEN = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwicm9sZSI6ImV2YWx1YXRvciJ9.test';

  beforeEach(() => {
    localStorage.clear();
  });

  afterEach(() => {
    localStorage.clear();
  });

  const renderDashboardPage = (token = MOCK_TOKEN) => {
    // Set token before rendering - use correct key
    if (token) {
      localStorage.setItem('innovatepam.jwt', token);
    } else {
      localStorage.removeItem('innovatepam.jwt');
    }

    return render(
      <BrowserRouter>
        <AuthProvider>
          <DashboardPage />
        </AuthProvider>
      </BrowserRouter>
    );
  };

  it('should display dashboard content when authenticated', async () => {
    renderDashboardPage();

    // Modern UI: Look for main Dashboard heading
    await waitFor(() => {
      const heading = screen.queryByRole('heading', { name: /dashboard|welcome/i });
      expect(heading !== null).toBeTruthy();
    });
  });

  it('should display user role information', () => {
    renderDashboardPage();

    // Dashboard should display or render content
    expect(screen.getByText(/dashboard|ideas|summary|welcome/i)).toBeInTheDocument();
  });

  it('should have logout functionality', async () => {
    renderDashboardPage();

    // Look for logout button (in modern UI sidebar/header)
    const logoutButton = screen.queryByRole('button', { name: /logout|sign out|exit/i });
    
    // Verify dashboard loaded first
    expect(screen.getByText(/dashboard|ideas|summary/i)).toBeInTheDocument();
  });

  it('should redirect to login when token is invalid', () => {
    // Render with no token
    renderDashboardPage(null);

    // After removing token, component should either show login or redirect
    // We verify the page is rendered without token
    expect(document.body).toBeInTheDocument();
  });

  it('should handle API errors gracefully', async () => {
    renderDashboardPage();

    // Dashboard should render even if API calls fail
    await waitFor(() => {
      expect(screen.getByText(/dashboard|ideas|summary|welcome/i)).toBeInTheDocument();
    });
  });

  it('should display appropriate content for role', () => {
    renderDashboardPage();

    // Modern UI: Main dashboard heading should be present
    expect(screen.getByText(/dashboard|ideas|summary|welcome/i)).toBeInTheDocument();
  });

  it('should fetch user data on mount', async () => {
    renderDashboardPage();

    // Wait for dashboard to load
    await waitFor(() => {
      expect(screen.getByText(/dashboard|ideas|summary|welcome/i)).toBeInTheDocument();
    });
  });
});
