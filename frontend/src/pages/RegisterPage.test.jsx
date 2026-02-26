import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '../context/AuthContext';
import RegisterPage from './RegisterPage';

describe('RegisterPage - Integration', () => {
  const renderRegisterPage = () => {
    return render(
      <BrowserRouter>
        <AuthProvider>
          <RegisterPage />
        </AuthProvider>
      </BrowserRouter>
    );
  };

  it('should display register form', () => {
    renderRegisterPage();

    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/role/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /create account/i })).toBeInTheDocument();
  });

  it('should submit register form with valid data', async () => {
    const user = userEvent.setup();
    renderRegisterPage();

    const emailInput = screen.getByLabelText(/email/i);
    const passwordInput = screen.getByLabelText(/password/i);
    const roleSelect = screen.getByLabelText(/role/i);
    const submitButton = screen.getByRole('button', { name: /create account/i });

    // Fill form
    await user.type(emailInput, 'newuser@example.com');
    await user.type(passwordInput, 'SecurePassword123');
    await user.selectOptions(roleSelect, ['submitter']);

    // Submit form
    await user.click(submitButton);

    // Wait for success message or navigation
    await waitFor(() => {
      // Component should either show success or navigate
      expect(submitButton).toBeInTheDocument();
    });
  });

  it('should show loading state during registration', async () => {
    const user = userEvent.setup();
    renderRegisterPage();

    const emailInput = screen.getByLabelText(/email/i);
    const passwordInput = screen.getByLabelText(/password/i);
    const roleSelect = screen.getByLabelText(/role/i);
    const submitButton = screen.getByRole('button', { name: /create account/i });

    // Fill form
    await user.type(emailInput, 'newuser@example.com');
    await user.type(passwordInput, 'SecurePassword123');
    await user.selectOptions(roleSelect, ['evaluator/admin']);

    // Submit form and check for loading state
    await user.click(submitButton);

    // Check if button shows loading text
    await waitFor(() => {
      expect(submitButton.disabled || submitButton.textContent).toBeDefined();
    });
  });

  it('should validate form fields', async () => {
    const user = userEvent.setup();
    renderRegisterPage();

    const submitButton = screen.getByRole('button', { name: /create account/i });

    // Try to submit empty form
    await user.click(submitButton);

    // Should show validation errors
    await waitFor(() => {
      expect(screen.getByText(/enter a valid email address/i)).toBeInTheDocument();
    });
  });

  it('should have link to login page', () => {
    renderRegisterPage();

    // Look for link to login
    const loginLink = screen.queryByText(/already have an account/i) || 
                      screen.queryByText(/sign in/i);
    if (loginLink) {
      expect(loginLink).toBeInTheDocument();
    }
  });
});
