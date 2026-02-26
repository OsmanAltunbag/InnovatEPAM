import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, act, waitFor } from '@testing-library/react';
import { useIdeas } from './useIdeas';

// Mock useAuth hook
vi.mock('./useAuth', () => ({
  default: vi.fn(() => ({
    isSessionReady: true,
    isAuthenticated: true,
    token: 'test-token',
    user: { email: 'test@example.com', role: 'submitter' },
  })),
}));

// Mock ideaService
vi.mock('../services/ideaService', () => ({
  getIdeas: vi.fn(() =>
    Promise.resolve({
      content: [
        { id: 1, title: 'Test Idea 1', status: 'SUBMITTED' },
        { id: 2, title: 'Test Idea 2', status: 'UNDER_REVIEW' },
      ],
      pageable: {
        pageNumber: 0,
        pageSize: 10,
        totalElements: 2,
        totalPages: 1,
      },
    })
  ),
  getIdeaById: vi.fn((id) =>
    Promise.resolve({
      id,
      title: `Idea ${id}`,
      description: 'Test Description',
      status: 'SUBMITTED',
      category: 'Innovation',
    })
  ),
  createIdea: vi.fn((data, file) =>
    Promise.resolve({
      id: 3,
      ...data,
      status: 'SUBMITTED',
      hasAttachment: !!file,
    })
  ),
  updateIdeaStatus: vi.fn((id, data) =>
    Promise.resolve({
      id,
      ...data,
      status: data.newStatus,
    })
  ),
  addIdeaComment: vi.fn((id, comment) =>
    Promise.resolve({
      id: 1,
      comment,
      evaluatorEmail: 'evaluator@example.com',
      createdAt: new Date().toISOString(),
    })
  ),
  getEvaluationHistory: vi.fn((id) =>
    Promise.resolve([
      { id: 1, comment: 'Good idea', evaluatorEmail: 'evaluator@example.com' },
    ])
  ),
}));

describe('useIdeas hook', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('initialization', () => {
    it('should initialize with empty ideas array', () => {
      // Arrange & Act
      const { result } = renderHook(() => useIdeas({ autoLoad: false }));
      // Assert
      expect(result.current.ideas).toEqual([]);
      expect(result.current.selectedIdea).toBeNull();
      expect(result.current.loading).toBe(false);
      expect(result.current.error).toBeNull();
    });

    it('should initialize with pagination info', () => {
      // Arrange & Act
      const { result } = renderHook(() => useIdeas({ autoLoad: false }));
      // Assert
      expect(result.current.pagination).toEqual({
        pageNumber: 0,
        pageSize: 10,
        totalElements: 0,
        totalPages: 0,
      });
    });

    it('should auto-load ideas when autoLoad is true', async () => {
      // Arrange & Act
      const { result } = renderHook(() => useIdeas({ autoLoad: true }));
      // Assert
      await waitFor(() => {
        expect(result.current.ideas.length).toBeGreaterThan(0);
      });
    });

    it('should not auto-load ideas when autoLoad is false', () => {
      // Arrange & Act
      const { result } = renderHook(() => useIdeas({ autoLoad: false }));
      // Assert
      expect(result.current.ideas).toEqual([]);
    });
  });

  describe('fetchIdeas', () => {
    it('should set loading to true during fetch', async () => {
      // Arrange
      const { result } = renderHook(() => useIdeas({ autoLoad: false }));
      // Act
      act(() => {
        result.current.fetchIdeas();
      });
      // Assert
      expect(result.current.loading).toBe(true);
    });

    it('should fetch ideas successfully', async () => {
      // Arrange
      const { result } = renderHook(() => useIdeas({ autoLoad: false }));
      // Act
      await act(async () => {
        await result.current.fetchIdeas();
      });
      // Assert
      expect(result.current.ideas.length).toBe(2);
      expect(result.current.ideas[0].title).toBe('Test Idea 1');
    });

    it('should update pagination info after fetch', async () => {
      // Arrange
      const { result } = renderHook(() => useIdeas({ autoLoad: false }));
      // Act
      await act(async () => {
        await result.current.fetchIdeas();
      });
      // Assert
      expect(result.current.pagination.totalElements).toBe(2);
      expect(result.current.pagination.pageSize).toBe(10);
    });

    it('should set error on fetch failure', async () => {
      // Arrange
      const { result } = renderHook(() => useIdeas({ autoLoad: false }));
      const ideaService = await import('../services/ideaService');
      ideaService.getIdeas.mockRejectedValueOnce({
        response: { data: { message: 'Fetch failed' } },
      });
      // Act
      await act(async () => {
        await result.current.fetchIdeas();
      });
      // Assert
      expect(result.current.error).toContain('Fetch failed');
      expect(result.current.loading).toBe(false);
    });

    it('should set loading to false after fetch completes', async () => {
      // Arrange
      const { result } = renderHook(() => useIdeas({ autoLoad: false }));
      // Act
      await act(async () => {
        await result.current.fetchIdeas();
      });
      // Assert
      expect(result.current.loading).toBe(false);
    });
  });

  describe('fetchIdeaById', () => {
    it('should fetch a single idea by ID', async () => {
      // Arrange
      const { result } = renderHook(() => useIdeas({ autoLoad: false }));
      // Act
      await act(async () => {
        await result.current.fetchIdeaById(1);
      });
      // Assert
      expect(result.current.selectedIdea.id).toBe(1);
      expect(result.current.selectedIdea.title).toBe('Idea 1');
    });

    it('should set loading to true during single idea fetch', async () => {
      // Arrange
      const { result } = renderHook(() => useIdeas({ autoLoad: false }));
      // Act
      act(() => {
        result.current.fetchIdeaById(1);
      });
      // Assert
      expect(result.current.loading).toBe(true);
    });

    it('should set error on single idea fetch failure', async () => {
      // Arrange
      const { result } = renderHook(() => useIdeas({ autoLoad: false }));
      const ideaService = await import('../services/ideaService');
      ideaService.getIdeaById.mockRejectedValueOnce({
        response: { data: { message: 'Idea not found' } },
      });
      // Act
      await act(async () => {
        try {
          await result.current.fetchIdeaById(999);
        } catch (e) {
          // Expected
        }
      });
      // Assert
      expect(result.current.error).toContain('Idea not found');
    });
  });

  describe('createIdea', () => {
    it('should create a new idea', async () => {
      // Arrange
      const { result } = renderHook(() => useIdeas({ autoLoad: false }));
      await act(async () => {
        await result.current.fetchIdeas();
      });
      const ideaData = { title: 'New Idea', description: 'Test', category: 'Innovation' };
      // Act
      await act(async () => {
        await result.current.createIdea(ideaData, null);
      });
      // Assert
      expect(result.current.ideas[0].title).toBe('New Idea');
    });

    it('should add created idea to the beginning of list', async () => {
      // Arrange
      const { result } = renderHook(() => useIdeas({ autoLoad: false }));
      await act(async () => {
        await result.current.fetchIdeas();
      });
      const initialLength = result.current.ideas.length;
      const ideaData = { title: 'New Idea', description: 'Test', category: 'Innovation' };
      // Act
      await act(async () => {
        await result.current.createIdea(ideaData, null);
      });
      // Assert
      expect(result.current.ideas.length).toBe(initialLength + 1);
      expect(result.current.ideas[0].id).toBe(3);
    });

    it('should set loading to true during idea creation', async () => {
      // Arrange
      const { result } = renderHook(() => useIdeas({ autoLoad: false }));
      const ideaData = { title: 'New Idea', description: 'Test', category: 'Innovation' };
      // Act
      act(() => {
        result.current.createIdea(ideaData, null);
      });
      // Assert
      expect(result.current.loading).toBe(true);
    });

    it('should set error on idea creation failure', async () => {
      // Arrange
      const { result } = renderHook(() => useIdeas({ autoLoad: false }));
      const ideaService = await import('../services/ideaService');
      ideaService.createIdea.mockRejectedValueOnce({
        response: { data: { message: 'Creation failed' } },
      });
      const ideaData = { title: 'New Idea', description: 'Test', category: 'Innovation' };
      // Act
      await act(async () => {
        try {
          await result.current.createIdea(ideaData, null);
        } catch (e) {
          // Expected
        }
      });
      // Assert
      expect(result.current.error).toContain('Creation failed');
    });

    it('should handle file upload with idea creation', async () => {
      // Arrange
      const { result } = renderHook(() => useIdeas({ autoLoad: false }));
      const ideaData = { title: 'New Idea', description: 'Test', category: 'Innovation' };
      const mockFile = new File(['test'], 'test.pdf', { type: 'application/pdf' });
      // Act
      await act(async () => {
        await result.current.createIdea(ideaData, mockFile);
      });
      // Assert
      expect(result.current.ideas[0].hasAttachment).toBe(true);
    });
  });

  describe('updateStatus', () => {
    it('should update status of an idea', async () => {
      // Arrange
      const { result } = renderHook(() => useIdeas({ autoLoad: false }));
      await act(async () => {
        await result.current.fetchIdeas();
      });
      const statusData = { newStatus: 'UNDER_REVIEW', comment: 'Moving to review' };
      // Act
      await act(async () => {
        await result.current.updateStatus(1, statusData);
      });
      // Assert
      expect(result.current.loading).toBe(false);
    });

    it('should update selected idea if it matches', async () => {
      // Arrange
      const { result } = renderHook(() => useIdeas({ autoLoad: false }));
      await act(async () => {
        await result.current.fetchIdeaById(1);
      });
      const statusData = { newStatus: 'UNDER_REVIEW', comment: 'Moving to review' };
      // Act
      await act(async () => {
        await result.current.updateStatus(1, statusData);
      });
      // Assert
      expect(result.current.selectedIdea.status).toBe('UNDER_REVIEW');
    });

    it('should set error on status update failure', async () => {
      // Arrange
      const { result } = renderHook(() => useIdeas({ autoLoad: false }));
      const ideaService = await import('../services/ideaService');
      ideaService.updateIdeaStatus.mockRejectedValueOnce({
        response: { data: { message: 'Update failed' } },
      });
      const statusData = { newStatus: 'UNDER_REVIEW', comment: 'Moving to review' };
      // Act
      await act(async () => {
        try {
          await result.current.updateStatus(1, statusData);
        } catch (e) {
          // Expected
        }
      });
      // Assert
      expect(result.current.error).toContain('Update failed');
    });
  });

  describe('addComment', () => {
    it('should add a comment to an idea', async () => {
      // Arrange
      const { result } = renderHook(() => useIdeas({ autoLoad: false }));
      // Act
      await act(async () => {
        await result.current.addComment(1, 'Great idea!');
      });
      // Assert
      expect(result.current.loading).toBe(false);
    });

    it('should set error on comment addition failure', async () => {
      // Arrange
      const { result } = renderHook(() => useIdeas({ autoLoad: false }));
      const ideaService = await import('../services/ideaService');
      ideaService.addIdeaComment.mockRejectedValueOnce({
        response: { data: { message: 'Comment failed' } },
      });
      // Act
      await act(async () => {
        try {
          await result.current.addComment(1, 'Great idea!');
        } catch (e) {
          // Expected
        }
      });
      // Assert
      expect(result.current.error).toContain('Comment failed');
    });
  });

  describe('fetchEvaluations', () => {
    it('should fetch evaluation history for an idea', async () => {
      // Arrange
      const { result } = renderHook(() => useIdeas({ autoLoad: false }));
      // Act
      const evaluations = await act(async () => {
        return await result.current.fetchEvaluations(1);
      });
      // Assert
      expect(Array.isArray(evaluations)).toBe(true);
    });

    it('should set error on evaluation fetch failure', async () => {
      // Arrange
      const { result } = renderHook(() => useIdeas({ autoLoad: false }));
      const ideaService = await import('../services/ideaService');
      ideaService.getEvaluationHistory.mockRejectedValueOnce({
        response: { data: { message: 'Evaluation fetch failed' } },
      });
      // Act
      await act(async () => {
        try {
          await result.current.fetchEvaluations(1);
        } catch (e) {
          // Expected
        }
      });
      // Assert
      expect(result.current.error).toContain('Evaluation fetch failed');
    });
  });

  describe('error handling', () => {
    it('should clear error on successful operation', async () => {
      // Arrange
      const { result } = renderHook(() => useIdeas({ autoLoad: false }));
      // Act
      await act(async () => {
        await result.current.fetchIdeas();
      });
      // Assert
      expect(result.current.error).toBeNull();
    });

    it('should use generic error message when response data is missing', async () => {
      // Arrange
      const { result } = renderHook(() => useIdeas({ autoLoad: false }));
      const ideaService = await import('../services/ideaService');
      ideaService.getIdeas.mockRejectedValueOnce(new Error('Network error'));
      // Act
      await act(async () => {
        await result.current.fetchIdeas();
      });
      // Assert
      expect(result.current.error).toContain('Failed to fetch ideas');
    });
  });
});
