import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import RegisterForm from './RegisterForm';

describe('RegisterForm', () => {
  it('should render form with email, password, and role fields', () => {
    const mockOnSubmit = vi.fn();
    render(<RegisterForm onSubmit={mockOnSubmit} loading={false} />);

    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /create account/i })).toBeInTheDocument();
  });

  it('should display validation errors when form is submitted with invalid data', () => {
    const mockOnSubmit = vi.fn();
    const { container } = render(<RegisterForm onSubmit={mockOnSubmit} loading={false} />);

    const form = container.querySelector('form');
    // Submit empty form
    fireEvent.submit(form);

    // Should show email validation error
    expect(screen.getByText(/enter a valid email address/i)).toBeInTheDocument();

    // Should show password validation error
    expect(screen.getByText(/password must be at least 8 characters/i)).toBeInTheDocument();

    // Should show role validation error
    expect(screen.getByText(/select a role/i)).toBeInTheDocument();

    // Should not call onSubmit
    expect(mockOnSubmit).not.toHaveBeenCalled();
  });

  it('should validate email field', async () => {
    const user = userEvent.setup();
    const mockOnSubmit = vi.fn();
    const { container } = render(<RegisterForm onSubmit={mockOnSubmit} loading={false} />);

    const emailInput = screen.getByLabelText(/email/i);
    const passwordInput = screen.getByLabelText(/password/i);
    const roleSelect = screen.getByLabelText(/role/i);
    const form = container.querySelector('form');

    // Invalid email
    await user.type(emailInput, 'invalid-email');
    fireEvent.submit(form);
    
    // Error should appear
    expect(screen.getByText(/enter a valid email address/i)).toBeInTheDocument();

    // Clear and enter valid email, add other required fields
    await user.clear(emailInput);
    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'ValidPassword123');
    await user.selectOptions(roleSelect, ['submitter']);
    
    // Re-submit with valid email
    fireEvent.submit(form);
    
    // Error should be gone and submit should be called
    expect(screen.queryByText(/enter a valid email address/i)).not.toBeInTheDocument();
    expect(mockOnSubmit).toHaveBeenCalled();
  });

  it('should validate password field', async () => {
    const user = userEvent.setup();
    const mockOnSubmit = vi.fn();
    const { container } = render(<RegisterForm onSubmit={mockOnSubmit} loading={false} />);

    const emailInput = screen.getByLabelText(/email/i);
    const passwordInput = screen.getByLabelText(/password/i);
    const roleSelect = screen.getByLabelText(/role/i);
    const form = container.querySelector('form');

    // Short password
    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'short');
    await user.selectOptions(roleSelect, ['submitter']);
    fireEvent.submit(form);
    
    // Error should appear
    expect(screen.getByText(/password must be at least 8 characters/i)).toBeInTheDocument();

    // Clear and enter valid password
    await user.clear(passwordInput);
    await user.type(passwordInput, 'ValidPassword123');
    
    // Re-submit
    fireEvent.submit(form);
    
    // Error should be gone and submit should be called
    expect(screen.queryByText(/password must be at least 8 characters/i)).not.toBeInTheDocument();
    expect(mockOnSubmit).toHaveBeenCalled();
  });

  it('should call onSubmit with form data when validation passes', async () => {
    const user = userEvent.setup();
    const mockOnSubmit = vi.fn();
    const { container } = render(<RegisterForm onSubmit={mockOnSubmit} loading={false} />);

    const emailInput = screen.getByLabelText(/email/i);
    const passwordInput = screen.getByLabelText(/password/i);
    const roleSelect = screen.getByLabelText(/role/i);
    const form = container.querySelector('form');

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'ValidPass123');
    await user.selectOptions(roleSelect, ['submitter']);
    
    fireEvent.submit(form);

    // Check submission
    expect(mockOnSubmit).toHaveBeenCalledWith({
      email: 'test@example.com',
      password: 'ValidPass123',
      role: 'submitter'
    });
  });

  it('should disable submit button when loading prop is true', () => {
    const mockOnSubmit = vi.fn();
    render(<RegisterForm onSubmit={mockOnSubmit} loading={true} />);

    const submitButton = screen.getByRole('button', { name: /creating account/i });
    expect(submitButton).toBeDisabled();
    expect(submitButton).toHaveTextContent('Creating account...');
  });

  it('should enable submit button when loading prop is false', () => {
    const mockOnSubmit = vi.fn();
    render(<RegisterForm onSubmit={mockOnSubmit} loading={false} />);

    const submitButton = screen.getByRole('button', { name: /create account/i });
    expect(submitButton).not.toBeDisabled();
  });

  it('should update input values as user types', async () => {
    const user = userEvent.setup();
    const mockOnSubmit = vi.fn();
    render(<RegisterForm onSubmit={mockOnSubmit} loading={false} />);

    const emailInput = screen.getByLabelText(/email/i);
    const passwordInput = screen.getByLabelText(/password/i);

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'mypassword123');

    expect(emailInput.value).toBe('test@example.com');
    expect(passwordInput.value).toBe('mypassword123');
  });
});
