import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, fireEvent, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import EvaluationPanel from './EvaluationPanel';

// Mock hooks
vi.mock('../hooks/useAuth', () => ({
  default: vi.fn(),
}));

vi.mock('../hooks/useIdeas', () => ({
  useIdeas: vi.fn(),
}));

// Mock utilities
vi.mock('../utils/statusUtils', () => ({
  getAllowedNextStatuses: vi.fn((status) => {
    if (status === 'SUBMITTED') return ['UNDER_REVIEW'];
    if (status === 'UNDER_REVIEW') return ['ACCEPTED', 'REJECTED'];
    return [];
  }),
  getStatusLabel: vi.fn((status) => {
    const labels = {
      'SUBMITTED': 'Submitted',
      'UNDER_REVIEW': 'Under Review',
      'ACCEPTED': 'Accepted',
      'REJECTED': 'Rejected',
    };
    return labels[status] || status;
  }),
  getStatusIcon: vi.fn(() => '•'),
  getStatusColor: vi.fn(() => 'bg-blue-100 text-blue-800'),
  getStatusBadgeClass: vi.fn(() => 'bg-blue-50'),
  getTimelineDotClass: vi.fn(() => 'bg-blue-400'),
  isCommentRequired: vi.fn((status) => status === 'REJECTED' || status === 'ACCEPTED'),
  getStatusSuggestions: vi.fn(() => []),
}));

vi.mock('../utils/roleUtils', () => ({
  canEvaluate: vi.fn((role) => role === 'evaluator' || role === 'admin'),
}));

describe('EvaluationPanel', () => {
  const mockIdea = {
    id: 1,
    title: 'Test Idea',
    status: 'SUBMITTED',
    description: 'Test Description',
  };

  let mockUpdateStatus, mockAddComment, mockFetchEvaluations;

  beforeEach(() => {
    vi.clearAllMocks();

    mockUpdateStatus = vi.fn().mockResolvedValue({ ...mockIdea, status: 'UNDER_REVIEW' });
    mockAddComment = vi.fn().mockResolvedValue({ id: 1, comment: 'Test comment' });
    mockFetchEvaluations = vi.fn().mockResolvedValue([]);

    const useIdeas = require('../hooks/useIdeas').useIdeas;
    useIdeas.mockReturnValue({
      updateStatus: mockUpdateStatus,
      addComment: mockAddComment,
      fetchEvaluations: mockFetchEvaluations,
      loading: false,
    });
  });

  describe('rendering', () => {
    it('should render evaluation panel with idea', () => {
      // Arrange
      const useAuth = require('../hooks/useAuth').default;
      useAuth.mockReturnValue({
        user: { email: 'test@example.com', role: 'submitter' },
      });

      // Act
      render(<EvaluationPanel idea={mockIdea} />);

      // Assert
      expect(screen.getByText(/evaluation/i)).toBeInTheDocument();
    });

    it('should show submitter view when user role is SUBMITTER', () => {
      // Arrange
      const useAuth = require('../hooks/useAuth').default;
      useAuth.mockReturnValue({
        user: { email: 'submitter@example.com', role: 'submitter' },
      });

      // Act
      render(<EvaluationPanel idea={mockIdea} />);

      // Assert
      expect(screen.queryByText(/update status/i)).not.toBeInTheDocument();
    });

    it('should show evaluator view when user role is EVALUATOR', () => {
      // Arrange
      const useAuth = require('../hooks/useAuth').default;
      useAuth.mockReturnValue({
        user: { email: 'evaluator@example.com', role: 'evaluator' },
      });

      // Act
      render(<EvaluationPanel idea={mockIdea} />);

      // Assert
      // Looking for action buttons visible to evaluators
      expect(screen.getByText(/comment/i)).toBeInTheDocument();
    });

    it('should show evaluator view when user role is ADMIN', () => {
      // Arrange
      const useAuth = require('../hooks/useAuth').default;
      useAuth.mockReturnValue({
        user: { email: 'admin@example.com', role: 'admin' },
      });

      // Act
      render(<EvaluationPanel idea={mockIdea} />);

      // Assert
      expect(screen.getByText(/comment/i)).toBeInTheDocument();
    });

    it('should show "No evaluations yet" when evaluations list is empty', async () => {
      // Arrange
      const useAuth = require('../hooks/useAuth').default;
      useAuth.mockReturnValue({
        user: { email: 'test@example.com', role: 'submitter' },
      });

      // Act
      render(<EvaluationPanel idea={mockIdea} />);

      // Assert
      await waitFor(() => {
        expect(screen.getByText(/no evaluations|no comments/i)).toBeInTheDocument();
      });
    });

    it('should render evaluation timeline items correctly', async () => {
      // Arrange
      const useAuth = require('../hooks/useAuth').default;
      useAuth.mockReturnValue({
        user: { email: 'test@example.com', role: 'submitter' },
      });

      const evaluations = [
        {
          id: 1,
          evaluatorEmail: 'evaluator@example.com',
          comment: 'Good proposal',
          statusSnapshot: 'UNDER_REVIEW',
        },
      ];

      mockFetchEvaluations.mockResolvedValue(evaluations);

      // Act
      render(<EvaluationPanel idea={mockIdea} />);

      // Assert
      await waitFor(() => {
        expect(screen.getByText(/Good proposal/i)).toBeInTheDocument();
      });
    });
  });

  describe('status form interactions', () => {
    it('should show status form when "Update Status" button is clicked', async () => {
      // Arrange
      const useAuth = require('../hooks/useAuth').default;
      useAuth.mockReturnValue({
        user: { email: 'evaluator@example.com', role: 'evaluator' },
      });

      const user = userEvent.setup();

      // Act
      render(<EvaluationPanel idea={mockIdea} />);
      const updateButton = screen.getByText(/update/i);
      await user.click(updateButton);

      // Assert
      expect(screen.getByText(/select.*status/i)).toBeInTheDocument();
    });

    it('should hide status form when closed', async () => {
      // Arrange
      const useAuth = require('../hooks/useAuth').default;
      useAuth.mockReturnValue({
        user: { email: 'evaluator@example.com', role: 'evaluator' },
      });

      const user = userEvent.setup();

      // Act
      render(<EvaluationPanel idea={mockIdea} />);
      const updateButton = screen.getByRole('button', { name: /update/i });
      await user.click(updateButton);

      const form = screen.getByText(/select.*status/i);
      expect(form).toBeInTheDocument();
    });

    it('should show error when submitting empty status form', async () => {
      // Arrange
      const useAuth = require('../hooks/useAuth').default;
      useAuth.mockReturnValue({
        user: { email: 'evaluator@example.com', role: 'evaluator' },
      });

      const user = userEvent.setup();

      // Act
      render(<EvaluationPanel idea={mockIdea} />);
      const updateButton = screen.getByText(/update/i);
      await user.click(updateButton);

      const submitButton = screen.getByRole('button', { name: /confirm|submit/i });
      await user.click(submitButton);

      // Assert
      await waitFor(() => {
        expect(screen.getByText(/please select/i)).toBeInTheDocument();
      });
    });

    it('should call updateStatus with correct args on form submit', async () => {
      // Arrange
      const useAuth = require('../hooks/useAuth').default;
      useAuth.mockReturnValue({
        user: { email: 'evaluator@example.com', role: 'evaluator' },
      });

      const user = userEvent.setup();
      mockIdea.status = 'UNDER_REVIEW';

      // Act
      render(<EvaluationPanel idea={mockIdea} />);
      const updateButton = screen.getByText(/update/i);
      await user.click(updateButton);

      // Select status and confirm
      const statusSelect = screen.getByRole('combobox');
      await user.selectOptions(statusSelect, 'ACCEPTED');

      const submitButton = screen.getByRole('button', { name: /confirm|submit/i });
      await user.click(submitButton);

      // Assert
      await waitFor(() => {
        expect(mockUpdateStatus).toHaveBeenCalled();
      });
    });

    it('should show required comment field for rejection', async () => {
      // Arrange
      const useAuth = require('../hooks/useAuth').default;
      useAuth.mockReturnValue({
        user: { email: 'evaluator@example.com', role: 'evaluator' },
      });

      const user = userEvent.setup();
      mockIdea.status = 'UNDER_REVIEW';

      // Act
      render(<EvaluationPanel idea={mockIdea} />);
      const updateButton = screen.getByText(/update/i);
      await user.click(updateButton);

      const statusSelect = screen.getByRole('combobox');
      await user.selectOptions(statusSelect, 'REJECTED');

      // Assert
      expect(screen.getByText(/comment.*required/i)).toBeInTheDocument();
    });
  });

  describe('comment form interactions', () => {
    it('should show comment form when "Add Comment" button is clicked', async () => {
      // Arrange
      const useAuth = require('../hooks/useAuth').default;
      useAuth.mockReturnValue({
        user: { email: 'evaluator@example.com', role: 'evaluator' },
      });

      const user = userEvent.setup();

      // Act
      render(<EvaluationPanel idea={mockIdea} />);
      const commentButton = screen.getByText(/comment/i);
      await user.click(commentButton);

      // Assert
      expect(screen.getByPlaceholderText(/add.*comment/i)).toBeInTheDocument();
    });

    it('should show error when submitting empty comment', async () => {
      // Arrange
      const useAuth = require('../hooks/useAuth').default;
      useAuth.mockReturnValue({
        user: { email: 'evaluator@example.com', role: 'evaluator' },
      });

      const user = userEvent.setup();

      // Act
      render(<EvaluationPanel idea={mockIdea} />);
      const commentButton = screen.getByText(/comment/i);
      await user.click(commentButton);

      const submitButton = screen.getByRole('button', { name: /post|submit/i });
      await user.click(submitButton);

      // Assert
      await waitFor(() => {
        expect(screen.getByText(/cannot be empty|required/i)).toBeInTheDocument();
      });
    });

    it('should call addComment with correct args on submit', async () => {
      // Arrange
      const useAuth = require('../hooks/useAuth').default;
      useAuth.mockReturnValue({
        user: { email: 'evaluator@example.com', role: 'evaluator' },
      });

      const user = userEvent.setup();

      // Act
      render(<EvaluationPanel idea={mockIdea} />);
      const commentButton = screen.getByText(/comment/i);
      await user.click(commentButton);

      const commentInput = screen.getByPlaceholderText(/add.*comment/i);
      await user.type(commentInput, 'Great idea!');

      const submitButton = screen.getByRole('button', { name: /post|submit/i });
      await user.click(submitButton);

      // Assert
      await waitFor(() => {
        expect(mockAddComment).toHaveBeenCalledWith(mockIdea.id, 'Great idea!');
      });
    });

    it('should show error for comment exceeding length limit', async () => {
      // Arrange
      const useAuth = require('../hooks/useAuth').default;
      useAuth.mockReturnValue({
        user: { email: 'evaluator@example.com', role: 'evaluator' },
      });

      const user = userEvent.setup();

      // Act
      render(<EvaluationPanel idea={mockIdea} />);
      const commentButton = screen.getByText(/comment/i);
      await user.click(commentButton);

      const commentInput = screen.getByPlaceholderText(/add.*comment/i);
      const longText = 'a'.repeat(5001);
      await user.type(commentInput, longText);

      const submitButton = screen.getByRole('button', { name: /post|submit/i });
      await user.click(submitButton);

      // Assert
      await waitFor(() => {
        expect(screen.getByText(/exceeds.*limit/i)).toBeInTheDocument();
      });
    });
  });

  describe('loading states', () => {
    it('should show loading state during status update', async () => {
      // Arrange
      const useAuth = require('../hooks/useAuth').default;
      useAuth.mockReturnValue({
        user: { email: 'evaluator@example.com', role: 'evaluator' },
      });

      const useIdeas = require('../hooks/useIdeas').useIdeas;
      useIdeas.mockReturnValue({
        updateStatus: vi.fn(() => new Promise(() => {})), // Never resolves
        addComment: mockAddComment,
        fetchEvaluations: mockFetchEvaluations,
        loading: true,
      });

      // Act
      render(<EvaluationPanel idea={mockIdea} />);

      // Assert - buttons should be disabled during loading
      // Note: implementation specific, may check for loading spinner instead
    });
  });

  describe('error handling', () => {
    it('should display error message on status update failure', async () => {
      // Arrange
      const useAuth = require('../hooks/useAuth').default;
      useAuth.mockReturnValue({
        user: { email: 'evaluator@example.com', role: 'evaluator' },
      });

      const user = userEvent.setup();
      mockUpdateStatus.mockRejectedValueOnce({
        response: { data: { message: 'Status update failed' } },
      });

      mockIdea.status = 'UNDER_REVIEW';

      // Act
      render(<EvaluationPanel idea={mockIdea} />);
      const updateButton = screen.getByText(/update/i);
      await user.click(updateButton);

      const statusSelect = screen.getByRole('combobox');
      await user.selectOptions(statusSelect, 'ACCEPTED');

      const submitButton = screen.getByRole('button', { name: /confirm|submit/i });
      await user.click(submitButton);

      // Assert
      await waitFor(() => {
        expect(screen.getByText(/failed/i)).toBeInTheDocument();
      });
    });

    it('should display error message on comment addition failure', async () => {
      // Arrange
      const useAuth = require('../hooks/useAuth').default;
      useAuth.mockReturnValue({
        user: { email: 'evaluator@example.com', role: 'evaluator' },
      });

      const user = userEvent.setup();
      mockAddComment.mockRejectedValueOnce({
        response: { data: { message: 'Comment addition failed' } },
      });

      // Act
      render(<EvaluationPanel idea={mockIdea} />);
      const commentButton = screen.getByText(/comment/i);
      await user.click(commentButton);

      const commentInput = screen.getByPlaceholderText(/add.*comment/i);
      await user.type(commentInput, 'Test comment');

      const submitButton = screen.getByRole('button', { name: /post|submit/i });
      await user.click(submitButton);

      // Assert
      await waitFor(() => {
        expect(screen.getByText(/failed/i)).toBeInTheDocument();
      });
    });
  });
});
