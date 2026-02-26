import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import IdeaForm from './IdeaForm';

// Mock react-router-dom
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: vi.fn(() => vi.fn()),
  };
});

// Mock useIdeas hook
vi.mock('../hooks/useIdeas', () => ({
  useIdeas: vi.fn(() => ({
    createIdea: vi.fn().mockResolvedValue({ id: 1, title: 'Test Idea' }),
    loading: false,
    error: null,
  })),
}));

// Mock useFileUpload hook
vi.mock('../hooks/useFileUpload', () => ({
  useFileUpload: vi.fn(() => ({
    file: null,
    fileError: null,
    handleFileSelect: vi.fn(),
    clearFile: vi.fn(),
  })),
}));

// Mock FileUpload component
vi.mock('../components/FileUpload', () => ({
  default: () => <div data-testid="file-upload">File Upload Component</div>,
}));

describe('IdeaForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('rendering', () => {
    it('should render all form fields', () => {
      // Arrange & Act
      render(
        <BrowserRouter>
          <IdeaForm />
        </BrowserRouter>
      );

      // Assert
      expect(screen.getByLabelText(/title/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/description/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/category/i)).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /submit/i })).toBeInTheDocument();
    });

    it('should render form title and description', () => {
      // Arrange & Act
      render(
        <BrowserRouter>
          <IdeaForm />
        </BrowserRouter>
      );

      // Assert
      expect(screen.getByText(/submit new idea/i)).toBeInTheDocument();
      expect(screen.getByText(/share your innovative ideas/i)).toBeInTheDocument();
    });

    it('should render file upload component', () => {
      // Arrange & Act
      render(
        <BrowserRouter>
          <IdeaForm />
        </BrowserRouter>
      );

      // Assert
      expect(screen.getByTestId('file-upload')).toBeInTheDocument();
    });

    it('should have required field indicators', () => {
      // Arrange & Act
      render(
        <BrowserRouter>
          <IdeaForm />
        </BrowserRouter>
      );

      // Assert - look for asterisks or "required" indicators
      const requiredIndicators = screen.getAllByText('*');
      expect(requiredIndicators.length).toBeGreaterThan(0);
    });
  });

  describe('validation', () => {
    it('should show validation error when title is empty', async () => {
      // Arrange
      const user = userEvent.setup();
      render(
        <BrowserRouter>
          <IdeaForm />
        </BrowserRouter>
      );

      // Act
      const submitButton = screen.getByRole('button', { name: /submit/i });
      await user.click(submitButton);

      // Assert
      await waitFor(() => {
        expect(screen.getByText(/title.*required/i)).toBeInTheDocument();
      });
    });

    it('should show validation error when description is empty', async () => {
      // Arrange
      const user = userEvent.setup();
      render(
        <BrowserRouter>
          <IdeaForm />
        </BrowserRouter>
      );

      // Act
      const titleInput = screen.getByLabelText(/title/i);
      await user.type(titleInput, 'Test Title');

      const submitButton = screen.getByRole('button', { name: /submit/i });
      await user.click(submitButton);

      // Assert
      await waitFor(() => {
        expect(screen.getByText(/description.*required/i)).toBeInTheDocument();
      });
    });

    it('should show validation error when category is empty', async () => {
      // Arrange
      const user = userEvent.setup();
      render(
        <BrowserRouter>
          <IdeaForm />
        </BrowserRouter>
      );

      // Act
      const titleInput = screen.getByLabelText(/title/i);
      await user.type(titleInput, 'Test Title');

      const descriptionInput = screen.getByLabelText(/description/i);
      await user.type(descriptionInput, 'Test Description');

      const submitButton = screen.getByRole('button', { name: /submit/i });
      await user.click(submitButton);

      // Assert
      await waitFor(() => {
        expect(screen.getByText(/category.*required/i)).toBeInTheDocument();
      });
    });

    it('should show validation error when title exceeds max length', async () => {
      // Arrange
      const user = userEvent.setup();
      render(
        <BrowserRouter>
          <IdeaForm />
        </BrowserRouter>
      );

      // Act
      const titleInput = screen.getByLabelText(/title/i);
      const longTitle = 'a'.repeat(256);
      await user.type(titleInput, longTitle);

      const submitButton = screen.getByRole('button', { name: /submit/i });
      await user.click(submitButton);

      // Assert
      await waitFor(() => {
        expect(screen.getByText(/title.*exceed.*255/i)).toBeInTheDocument();
      });
    });

    it('should show validation error when category exceeds max length', async () => {
      // Arrange
      const user = userEvent.setup();
      render(
        <BrowserRouter>
          <IdeaForm />
        </BrowserRouter>
      );

      // Act
      const titleInput = screen.getByLabelText(/title/i);
      await user.type(titleInput, 'Test Title');

      const descriptionInput = screen.getByLabelText(/description/i);
      await user.type(descriptionInput, 'Test Description');

      const categoryInput = screen.getByLabelText(/category/i);
      const longCategory = 'a'.repeat(51);
      await user.type(categoryInput, longCategory);

      const submitButton = screen.getByRole('button', { name: /submit/i });
      await user.click(submitButton);

      // Assert
      await waitFor(() => {
        expect(screen.getByText(/category.*exceed.*50/i)).toBeInTheDocument();
      });
    });

    it('should clear error when user starts typing', async () => {
      // Arrange
      const user = userEvent.setup();
      render(
        <BrowserRouter>
          <IdeaForm />
        </BrowserRouter>
      );

      // Act - trigger validation error
      const submitButton = screen.getByRole('button', { name: /submit/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/title.*required/i)).toBeInTheDocument();
      });

      // Type in title field
      const titleInput = screen.getByLabelText(/title/i);
      await user.type(titleInput, 'Test');

      // Assert - error should be cleared
      await waitFor(() => {
        expect(screen.queryByText(/title.*required/i)).not.toBeInTheDocument();
      });
    });
  });

  describe('form submission', () => {
    it('should call createIdea with correct data on valid submit', async () => {
      // Arrange
      const user = userEvent.setup();
      const { useIdeas } = await import('../hooks/useIdeas');
      const mockCreateIdea = vi.fn().mockResolvedValue({ id: 1 });
      useIdeas.mockReturnValue({
        createIdea: mockCreateIdea,
        loading: false,
        error: null,
      });

      render(
        <BrowserRouter>
          <IdeaForm />
        </BrowserRouter>
      );

      // Act
      const titleInput = screen.getByLabelText(/title/i);
      await user.type(titleInput, 'Test Title');

      const descriptionInput = screen.getByLabelText(/description/i);
      await user.type(descriptionInput, 'Test Description');

      const categoryInput = screen.getByLabelText(/category/i);
      await user.type(categoryInput, 'Innovation');

      const submitButton = screen.getByRole('button', { name: /submit/i });
      await user.click(submitButton);

      // Assert
      await waitFor(() => {
        expect(mockCreateIdea).toHaveBeenCalledWith(
          expect.objectContaining({
            title: 'Test Title',
            description: 'Test Description',
            category: 'Innovation',
          }),
          null
        );
      });
    });

    it('should navigate to /ideas on successful submission', async () => {
      // Arrange
      const user = userEvent.setup();
      const { useNavigate } = await import('react-router-dom');
      const mockNavigate = vi.fn();
      useNavigate.mockReturnValue(mockNavigate);

      render(
        <BrowserRouter>
          <IdeaForm />
        </BrowserRouter>
      );

      // Act
      const titleInput = screen.getByLabelText(/title/i);
      await user.type(titleInput, 'Test Title');

      const descriptionInput = screen.getByLabelText(/description/i);
      await user.type(descriptionInput, 'Test Description');

      const categoryInput = screen.getByLabelText(/category/i);
      await user.type(categoryInput, 'Innovation');

      const submitButton = screen.getByRole('button', { name: /submit/i });
      await user.click(submitButton);

      // Assert
      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/ideas');
      });
    });

    it('should disable submit button while loading', async () => {
      // Arrange
      const { useIdeas } = await import('../hooks/useIdeas');
      useIdeas.mockReturnValue({
        createIdea: vi.fn(() => new Promise(() => {})),
        loading: true,
        error: null,
      });

      render(
        <BrowserRouter>
          <IdeaForm />
        </BrowserRouter>
      );

      // Act
      const submitButton = screen.getByRole('button', { name: /submit/i });

      // Assert
      expect(submitButton).toBeDisabled();
    });

    it('should not submit if file validation fails', async () => {
      // Arrange
      const user = userEvent.setup();
      const { useFileUpload } = await import('../hooks/useFileUpload');
      useFileUpload.mockReturnValue({
        file: null,
        fileError: 'Invalid file type',
        handleFileSelect: vi.fn(),
        clearFile: vi.fn(),
      });

      const { useIdeas } = await import('../hooks/useIdeas');
      const mockCreateIdea = vi.fn();
      useIdeas.mockReturnValue({
        createIdea: mockCreateIdea,
        loading: false,
        error: null,
      });

      render(
        <BrowserRouter>
          <IdeaForm />
        </BrowserRouter>
      );

      // Act
      const titleInput = screen.getByLabelText(/title/i);
      await user.type(titleInput, 'Test Title');

      const descriptionInput = screen.getByLabelText(/description/i);
      await user.type(descriptionInput, 'Test Description');

      const categoryInput = screen.getByLabelText(/category/i);
      await user.type(categoryInput, 'Innovation');

      const submitButton = screen.getByRole('button', { name: /submit/i });
      await user.click(submitButton);

      // Assert
      await waitFor(() => {
        expect(mockCreateIdea).not.toHaveBeenCalled();
      });
    });
  });

  describe('form interactions', () => {
    it('should update form data on input change', async () => {
      // Arrange
      const user = userEvent.setup();
      render(
        <BrowserRouter>
          <IdeaForm />
        </BrowserRouter>
      );

      // Act
      const titleInput = screen.getByLabelText(/title/i);
      await user.type(titleInput, 'My Idea Title');

      // Assert
      expect(titleInput).toHaveValue('My Idea Title');
    });

    it('should update description field', async () => {
      // Arrange
      const user = userEvent.setup();
      render(
        <BrowserRouter>
          <IdeaForm />
        </BrowserRouter>
      );

      // Act
      const descriptionInput = screen.getByLabelText(/description/i);
      await user.type(descriptionInput, 'Detailed description of the idea');

      // Assert
      expect(descriptionInput).toHaveValue('Detailed description of the idea');
    });

    it('should update category field', async () => {
      // Arrange
      const user = userEvent.setup();
      render(
        <BrowserRouter>
          <IdeaForm />
        </BrowserRouter>
      );

      // Act
      const categoryInput = screen.getByLabelText(/category/i);
      await user.type(categoryInput, 'Cost Reduction');

      // Assert
      expect(categoryInput).toHaveValue('Cost Reduction');
    });
  });

  describe('error handling', () => {
    it('should display API error on submission failure', async () => {
      // Arrange
      const user = userEvent.setup();
      const { useIdeas } = await import('../hooks/useIdeas');
      const mockCreateIdea = vi.fn().mockRejectedValue({
        response: { data: { message: 'Server error' } },
      });
      useIdeas.mockReturnValue({
        createIdea: mockCreateIdea,
        loading: false,
        error: 'Server error',
      });

      render(
        <BrowserRouter>
          <IdeaForm />
        </BrowserRouter>
      );

      // Act & Assert
      expect(screen.getByText(/server error/i)).toBeInTheDocument();
    });
  });
});
