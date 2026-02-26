import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import RoleSelector from './RoleSelector';

describe('RoleSelector', () => {
  it('should render role selector with default empty option', () => {
    const mockOnChange = vi.fn();
    render(<RoleSelector value="" onChange={mockOnChange} />);

    const select = screen.getByRole('combobox');
    expect(select).toBeInTheDocument();
    expect(select.value).toBe('');
  });

  it('should render all available roles', () => {
    const mockOnChange = vi.fn();
    render(<RoleSelector value="" onChange={mockOnChange} />);

    const options = screen.getAllByRole('option');
    expect(options[0]).toHaveTextContent('Select role');
    expect(options[1]).toHaveTextContent('Submitter');
    expect(options[2]).toHaveTextContent('Evaluator/Admin');
  });

  it('should call onChange when role is selected', async () => {
    const user = userEvent.setup();
    const mockOnChange = vi.fn();
    render(<RoleSelector value="" onChange={mockOnChange} />);

    const select = screen.getByRole('combobox');
    await user.selectOptions(select, 'submitter');

    expect(mockOnChange).toHaveBeenCalledWith('submitter');
  });

  it('should display selected role value', async () => {
    const user = userEvent.setup();
    const mockOnChange = vi.fn();
    const { rerender } = render(
      <RoleSelector value="" onChange={mockOnChange} />
    );

    const select = screen.getByRole('combobox');
    
    // Select submitter role
    await user.selectOptions(select, 'submitter');
    expect(mockOnChange).toHaveBeenCalledWith('submitter');

    // Rerender with selected value
    rerender(<RoleSelector value="submitter" onChange={mockOnChange} />);
    expect(screen.getByRole('combobox').value).toBe('submitter');
  });

  it('should allow switching between roles', async () => {
    const user = userEvent.setup();
    const mockOnChange = vi.fn();
    const { rerender } = render(
      <RoleSelector value="submitter" onChange={mockOnChange} />
    );

    const select = screen.getByRole('combobox');
    
    // Switch to evaluator/admin
    await user.selectOptions(select, 'evaluator/admin');
    expect(mockOnChange).toHaveBeenCalledWith('evaluator/admin');

    // Rerender with new value
    rerender(<RoleSelector value="evaluator/admin" onChange={mockOnChange} />);
    expect(screen.getByRole('combobox').value).toBe('evaluator/admin');
  });

  it('should display error message when error prop is provided', () => {
    const mockOnChange = vi.fn();
    const errorMessage = 'Please select a role';
    render(
      <RoleSelector value="" onChange={mockOnChange} error={errorMessage} />
    );

    expect(screen.getByText(errorMessage)).toBeInTheDocument();
  });

  it('should not display error message when error prop is not provided', () => {
    const mockOnChange = vi.fn();
    render(<RoleSelector value="" onChange={mockOnChange} />);

    // The component should have no error element when error is undefined
    const errorMessages = screen.queryAllByRole('paragraph');
    expect(errorMessages.length).toBe(0);
  });

  it('should have accessible label', () => {
    const mockOnChange = vi.fn();
    render(<RoleSelector value="" onChange={mockOnChange} />);

    // Label should be present and associated with the select
    const labels = screen.getAllByText(/role/i);
    const labelElement = labels.find(el => el.tagName === 'LABEL');
    expect(labelElement).toBeInTheDocument();
    expect(labelElement.classList.contains('block')).toBe(true);
  });
});
