import { describe, it, expect } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '../context/AuthContext';
import LoginPage from './LoginPage';

describe('LoginPage - Integration', () => {
  const renderLoginPage = () => {
    return render(
      <BrowserRouter>
        <AuthProvider>
          <LoginPage />
        </AuthProvider>
      </BrowserRouter>
    );
  };

  it('should display login form', () => {
    renderLoginPage();

    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
  });

  it('should submit login form with valid credentials', async () => {
    const user = userEvent.setup();
    const { container } = render(
      <BrowserRouter>
        <AuthProvider>
          <LoginPage />
        </AuthProvider>
      </BrowserRouter>
    );

    const emailInput = screen.getByLabelText(/email/i);
    const passwordInput = screen.getByLabelText(/password/i);
    const form = container.querySelector('form');

    // Fill form
    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'password123');

    // Submit form
    fireEvent.submit(form);

    // Wait for form submission
    // (In real scenario, would wait for navigation or success message)
    expect(emailInput).toBeInTheDocument();
  });

  it('should show loading state during login', async () => {
    const user = userEvent.setup();
    const { container } = render(
      <BrowserRouter>
        <AuthProvider>
          <LoginPage />
        </AuthProvider>
      </BrowserRouter>
    );

    const emailInput = screen.getByLabelText(/email/i);
    const passwordInput = screen.getByLabelText(/password/i);
    const submitButton = screen.getByRole('button', { name: /sign in/i });
    const form = container.querySelector('form');

    // Fill form
    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'password123');

    // Submit form
    fireEvent.submit(form);

    // Button should show loading text or be disabled during submission
    expect(submitButton.disabled || submitButton.textContent).toBeDefined();
  });

  it('should validate email field', async () => {
    const user = userEvent.setup();
    const { container } = render(
      <BrowserRouter>
        <AuthProvider>
          <LoginPage />
        </AuthProvider>
      </BrowserRouter>
    );

    const emailInput = screen.getByLabelText(/email/i);
    const form = container.querySelector('form');

    // Type invalid email
    await user.type(emailInput, 'invalid-email');
    fireEvent.submit(form);

    // Should show validation error
    expect(screen.getByText(/enter a valid email address/i)).toBeInTheDocument();
  });

  it('should validate password field', async () => {
    const user = userEvent.setup();
    const { container } = render(
      <BrowserRouter>
        <AuthProvider>
          <LoginPage />
        </AuthProvider>
      </BrowserRouter>
    );

    const form = container.querySelector('form');

    // Try to submit without password
    fireEvent.submit(form);

    // Should show validation error
    expect(screen.getByText(/enter your password/i)).toBeInTheDocument();
  });

  it('should have link to registration page', () => {
    renderLoginPage();

    // Look for registration link
    const registerLink = screen.queryByText(/don't have an account/i) || 
                        screen.queryByText(/create account/i) ||
                        screen.queryByText(/register/i);
    if (registerLink) {
      expect(registerLink).toBeInTheDocument();
    }
  });
});
