import { describe, it, expect, beforeAll, beforeEach, afterEach, afterAll } from 'vitest';
import { setupServer } from 'msw/node';
import { ideaHandlers } from './mocks/ideaHandlers';
import * as ideaService from '../services/ideaService';

// Setup MSW server for integration tests
const server = setupServer(...ideaHandlers);

const MOCK_TOKEN = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwicm9sZSI6ImV2YWx1YXRvciJ9.test';

beforeAll(() => server.listen());

beforeEach(() => {
  // Set JWT token in localStorage before each test
  localStorage.clear();
  localStorage.setItem('innovatepam.jwt', MOCK_TOKEN);
  server.resetHandlers();
});

afterEach(() => {
  server.resetHandlers();
  localStorage.clear();
});

afterAll(() => server.close());

describe('ideaService Integration Tests', () => {
  it('should fetch ideas with pagination', async () => {
    const response = await ideaService.getIdeas({ page: 0, size: 10 });
    
    expect(response).toBeDefined();
    expect(response.content).toBeInstanceOf(Array);
    expect(response.pageable).toBeDefined();
    expect(response.pageable.pageNumber).toBe(0);
  });

  it('should filter ideas by status', async () => {
    const response = await ideaService.getIdeas({ status: 'SUBMITTED' });
    
    expect(response.content).toBeInstanceOf(Array);
    response.content.forEach(idea => {
      expect(idea.status).toBe('SUBMITTED');
    });
  });

  it('should fetch idea by ID', async () => {
    const idea = await ideaService.getIdeaById(1);
    
    expect(idea).toBeDefined();
    expect(idea.id).toBe(1);
    expect(idea.title).toBeDefined();
    expect(idea.description).toBeDefined();
  });

  it('should create a new idea', async () => {
    const ideaData = {
      title: 'Test Idea',
      description: 'Test Description',
      category: 'Testing'
    };

    const newIdea = await ideaService.createIdea(ideaData);
    
    expect(newIdea).toBeDefined();
    expect(newIdea.title).toBe('Test Idea');
    expect(newIdea.status).toBe('SUBMITTED');
    expect(newIdea.evaluationCount).toBe(0);
  });

  it('should update idea status', async () => {
    const statusData = {
      newStatus: 'UNDER_REVIEW',
      comment: 'Moving to review'
    };

    const updatedIdea = await ideaService.updateIdeaStatus(2, statusData);
    
    expect(updatedIdea).toBeDefined();
    expect(updatedIdea.status).toBe('UNDER_REVIEW');
  });

  it('should add comment to idea', async () => {
    const comment = 'This is a test comment';
    const evaluation = await ideaService.addIdeaComment(1, comment);
    
    expect(evaluation).toBeDefined();
    expect(evaluation.comment).toBe(comment);
    expect(evaluation.statusSnapshot).toBeNull();
  });

  it('should fetch evaluation history', async () => {
    const response = await ideaService.getIdeaEvaluations(1);
    
    expect(response).toBeDefined();
    expect(response.ideaId).toBe(1);
    expect(response.evaluations).toBeInstanceOf(Array);
  });
});
