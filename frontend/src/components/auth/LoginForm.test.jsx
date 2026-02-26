import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import LoginForm from './LoginForm';

describe('LoginForm', () => {
  it('should render form with email and password fields', () => {
    const mockOnSubmit = vi.fn();
    render(<LoginForm onSubmit={mockOnSubmit} loading={false} />);

    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
  });

  it('should display validation errors when submitted with invalid data', () => {
    const mockOnSubmit = vi.fn();
    const { container } = render(<LoginForm onSubmit={mockOnSubmit} loading={false} />);

    const form = container.querySelector('form');
    // Submit empty form
    fireEvent.submit(form);

    // Should show validation errors
    expect(screen.getByText(/enter a valid email address/i)).toBeInTheDocument();
    expect(screen.getByText(/enter your password/i)).toBeInTheDocument();

    // Should not call onSubmit
    expect(mockOnSubmit).not.toHaveBeenCalled();
  });

  it('should validate email field', async () => {
    const user = userEvent.setup();
    const mockOnSubmit = vi.fn();
    const { container } = render(<LoginForm onSubmit={mockOnSubmit} loading={false} />);

    const emailInput = screen.getByLabelText(/email/i);
    const form = container.querySelector('form');

    // Invalid email - submit should show error
    await user.type(emailInput, 'invalid-email');
    fireEvent.submit(form);
    
    // Error should appear
    expect(screen.getByText(/enter a valid email address/i)).toBeInTheDocument();

    // Fix email and re-submit - should not have error
    await user.clear(emailInput);
    await user.type(emailInput, 'test@example.com');
    await user.type(screen.getByLabelText(/password/i), 'ValidPassword123');
    fireEvent.submit(form);
    
    // Error should be gone and submit should be called
    expect(screen.queryByText(/enter a valid email address/i)).not.toBeInTheDocument();
    expect(mockOnSubmit).toHaveBeenCalled();
  });

  it('should validate password field', async () => {
    const user = userEvent.setup();
    const mockOnSubmit = vi.fn();
    const { container } = render(<LoginForm onSubmit={mockOnSubmit} loading={false} />);

    const form = container.querySelector('form');

    // Empty password - submit should show error
    fireEvent.submit(form);
    
    // Error should appear
    expect(screen.getByText(/enter your password/i)).toBeInTheDocument();

    // Enter password and valid email, re-submit
    await user.type(screen.getByLabelText(/email/i), 'test@example.com');
    await user.type(screen.getByLabelText(/password/i), 'MyPassword123');
    fireEvent.submit(form);
    
    // Error should be gone and submit should be called
    expect(screen.queryByText(/enter your password/i)).not.toBeInTheDocument();
    expect(mockOnSubmit).toHaveBeenCalled();
  });

  it('should call onSubmit with form data when validation passes', async () => {
    const user = userEvent.setup();
    const mockOnSubmit = vi.fn();
    const { container } = render(<LoginForm onSubmit={mockOnSubmit} loading={false} />);

    const emailInput = screen.getByLabelText(/email/i);
    const passwordInput = screen.getByLabelText(/password/i);
    const form = container.querySelector('form');

    await user.type(emailInput, 'user@example.com');
    await user.type(passwordInput, 'MyPassword123');
    fireEvent.submit(form);

    expect(mockOnSubmit).toHaveBeenCalledWith({
      email: 'user@example.com',
      password: 'MyPassword123'
    });
  });

  it('should disable submit button when loading prop is true', () => {
    const mockOnSubmit = vi.fn();
    render(<LoginForm onSubmit={mockOnSubmit} loading={true} />);

    const submitButton = screen.getByRole('button', { name: /signing in/i });
    expect(submitButton).toBeDisabled();
    expect(submitButton).toHaveTextContent('Signing in...');
  });

  it('should enable submit button when loading prop is false', () => {
    const mockOnSubmit = vi.fn();
    render(<LoginForm onSubmit={mockOnSubmit} loading={false} />);

    const submitButton = screen.getByRole('button', { name: /sign in/i });
    expect(submitButton).not.toBeDisabled();
  });

  it('should update input values as user types', async () => {
    const user = userEvent.setup();
    const mockOnSubmit = vi.fn();
    render(<LoginForm onSubmit={mockOnSubmit} loading={false} />);

    const emailInput = screen.getByLabelText(/email/i);
    const passwordInput = screen.getByLabelText(/password/i);

    await user.type(emailInput, 'login@example.com');
    await user.type(passwordInput, 'securepass123');

    expect(emailInput.value).toBe('login@example.com');
    expect(passwordInput.value).toBe('securepass123');
  });

  it('should prevent form submission on enter key without validation', () => {
    const mockOnSubmit = vi.fn();
    const { container } = render(<LoginForm onSubmit={mockOnSubmit} loading={false} />);

    const form = container.querySelector('form');
    
    // Submit empty form
    fireEvent.submit(form);

    // Should show validation errors
    expect(screen.getByText(/enter a valid email address/i)).toBeInTheDocument();
    expect(screen.getByText(/enter your password/i)).toBeInTheDocument();

    expect(mockOnSubmit).not.toHaveBeenCalled();
  });
});
