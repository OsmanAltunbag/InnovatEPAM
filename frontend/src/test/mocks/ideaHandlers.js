import { http, HttpResponse } from 'msw';

const API_BASE = 'http://localhost:8080/api/v1';

// Mock data storage
let mockIdeas = [
  {
    id: 1,
    title: "Remote Work Policy Enhancement",
    category: "HR",
    status: "UNDER_REVIEW",
    submitterName: "alice@example.com",
    createdAt: "2026-02-20T10:00:00Z",
    hasAttachment: true,
    evaluationCount: 2
  },
  {
    id: 2,
    title: "Sustainable Packaging Initiative",
    category: "Sustainability",
    status: "SUBMITTED",
    submitterName: "bob@example.com",
    createdAt: "2026-02-22T14:30:00Z",
    hasAttachment: false,
    evaluationCount: 0
  },
  {
    id: 3,
    title: "Customer Portal Redesign",
    category: "Technology",
    status: "ACCEPTED",
    submitterName: "charlie@example.com",
    createdAt: "2026-02-15T09:15:00Z",
    hasAttachment: true,
    evaluationCount: 5
  }
];

let nextIdeaId = 4;

const mockEvaluations = {
  1: [
    {
      id: 1,
      evaluatorName: "evaluator@example.com",
      comment: "Moving to under review for detailed analysis",
      statusSnapshot: "UNDER_REVIEW",
      createdAt: "2026-02-21T11:00:00Z"
    },
    {
      id: 2,
      evaluatorName: "admin@example.com",
      comment: "Looks promising, need cost estimate",
      statusSnapshot: null,
      createdAt: "2026-02-23T15:30:00Z"
    }
  ],
  3: [
    {
      id: 3,
      evaluatorName: "evaluator@example.com",
      comment: "Excellent proposal with clear ROI",
      statusSnapshot: "ACCEPTED",
      createdAt: "2026-02-16T10:00:00Z"
    }
  ]
};

export const ideaHandlers = [
  // POST /api/v1/ideas - Create idea
  http.post(`${API_BASE}/ideas`, async ({ request }) => {
    const authHeader = request.headers.get('authorization');
    if (!authHeader) {
      return HttpResponse.json(
        { message: 'Authentication required' },
        { status: 401 }
      );
    }

    const formData = await request.formData();
    const title = formData.get('title');
    const description = formData.get('description');
    const category = formData.get('category');
    const file = formData.get('file');

    // Validation
    if (!title || title.trim().length === 0) {
      return HttpResponse.json(
        { message: 'Title is required' },
        { status: 400 }
      );
    }

    if (!description || description.trim().length === 0) {
      return HttpResponse.json(
        { message: 'Description is required' },
        { status: 400 }
      );
    }

    if (!category || category.trim().length === 0) {
      return HttpResponse.json(
        { message: 'Category is required' },
        { status: 400 }
      );
    }

    // Create new idea
    const newIdea = {
      id: nextIdeaId++,
      title: title.trim(),
      category: category.trim(),
      status: "SUBMITTED",
      submitterName: "test@example.com",
      createdAt: new Date().toISOString(),
      hasAttachment: file !== null,
      evaluationCount: 0
    };

    mockIdeas.unshift(newIdea);

    return HttpResponse.json(newIdea, { status: 201 });
  }),

  // GET /api/v1/ideas - Get all ideas (paginated)
  http.get(`${API_BASE}/ideas`, ({ request }) => {
    const authHeader = request.headers.get('authorization');
    if (!authHeader) {
      return HttpResponse.json(
        { message: 'Authentication required' },
        { status: 401 }
      );
    }

    const url = new URL(request.url);
    const page = parseInt(url.searchParams.get('page') || '0');
    const size = parseInt(url.searchParams.get('size') || '10');
    const status = url.searchParams.get('status');
    const category = url.searchParams.get('category');

    // Filter ideas
    let filteredIdeas = [...mockIdeas];
    if (status) {
      filteredIdeas = filteredIdeas.filter(idea => idea.status === status);
    }
    if (category) {
      filteredIdeas = filteredIdeas.filter(idea => idea.category === category);
    }

    // Pagination
    const start = page * size;
    const end = start + size;
    const paginatedIdeas = filteredIdeas.slice(start, end);

    return HttpResponse.json({
      content: paginatedIdeas,
      pageable: {
        pageNumber: page,
        pageSize: size,
        totalElements: filteredIdeas.length,
        totalPages: Math.ceil(filteredIdeas.length / size)
      }
    });
  }),

  // GET /api/v1/ideas/:id - Get idea detail
  http.get(`${API_BASE}/ideas/:id`, ({ request, params }) => {
    const authHeader = request.headers.get('authorization');
    if (!authHeader) {
      return HttpResponse.json(
        { message: 'Authentication required' },
        { status: 401 }
      );
    }

    const ideaId = parseInt(params.id);
    const idea = mockIdeas.find(i => i.id === ideaId);

    if (!idea) {
      return HttpResponse.json(
        { message: `Idea with ID ${ideaId} not found` },
        { status: 404 }
      );
    }

    // Return detailed view
    const detailIdea = {
      ...idea,
      description: "This is a detailed description of the idea. It includes comprehensive information about the proposal, expected outcomes, and implementation strategy.",
      updatedAt: idea.createdAt,
      attachment: idea.hasAttachment ? {
        id: 101,
        originalFilename: "proposal.pdf",
        fileSize: 2048576,
        createdAt: idea.createdAt
      } : null,
      evaluations: mockEvaluations[ideaId] || []
    };

    return HttpResponse.json(detailIdea);
  }),

  // PATCH /api/v1/ideas/:id/status - Update status
  http.patch(`${API_BASE}/ideas/:id/status`, async ({ request, params }) => {
    const authHeader = request.headers.get('authorization');
    if (!authHeader) {
      return HttpResponse.json(
        { message: 'Authentication required' },
        { status: 401 }
      );
    }

    const ideaId = parseInt(params.id);
    const idea = mockIdeas.find(i => i.id === ideaId);

    if (!idea) {
      return HttpResponse.json(
        { message: `Idea with ID ${ideaId} not found` },
        { status: 404 }
      );
    }

    const body = await request.json();
    const { newStatus, comment } = body;

    if (!newStatus) {
      return HttpResponse.json(
        { message: 'New status is required' },
        { status: 400 }
      );
    }

    // Validate status transition
    const validTransitions = {
      'SUBMITTED': ['UNDER_REVIEW', 'REJECTED'],
      'UNDER_REVIEW': ['ACCEPTED', 'REJECTED'],
      'ACCEPTED': [],
      'REJECTED': []
    };

    const allowed = validTransitions[idea.status] || [];
    if (!allowed.includes(newStatus)) {
      return HttpResponse.json(
        {
          message: `Cannot transition from ${idea.status} to ${newStatus}`,
          currentStatus: idea.status,
          attemptedStatus: newStatus
        },
        { status: 400 }
      );
    }

    // Check comment requirement for rejection
    if (newStatus === 'REJECTED' && (!comment || comment.trim().length === 0)) {
      return HttpResponse.json(
        { message: 'Comment is required when rejecting an idea' },
        { status: 400 }
      );
    }

    // Update status
    idea.status = newStatus;
    idea.evaluationCount += 1;

    // Add evaluation to mock data
    if (!mockEvaluations[ideaId]) {
      mockEvaluations[ideaId] = [];
    }
    mockEvaluations[ideaId].push({
      id: Date.now(),
      evaluatorName: "evaluator@example.com",
      comment: comment || `Status changed to ${newStatus}`,
      statusSnapshot: newStatus,
      createdAt: new Date().toISOString()
    });

    return HttpResponse.json(idea);
  }),

  // POST /api/v1/ideas/:id/comments - Add comment
  http.post(`${API_BASE}/ideas/:id/comments`, async ({ request, params }) => {
    const authHeader = request.headers.get('authorization');
    if (!authHeader) {
      return HttpResponse.json(
        { message: 'Authentication required' },
        { status: 401 }
      );
    }

    const ideaId = parseInt(params.id);
    const idea = mockIdeas.find(i => i.id === ideaId);

    if (!idea) {
      return HttpResponse.json(
        { message: `Idea with ID ${ideaId} not found` },
        { status: 404 }
      );
    }

    const body = await request.json();
    const { comment } = body;

    if (!comment || comment.trim().length === 0) {
      return HttpResponse.json(
        { message: 'Comment is required' },
        { status: 400 }
      );
    }

    // Create evaluation
    const evaluation = {
      id: Date.now(),
      evaluatorName: "evaluator@example.com",
      comment: comment.trim(),
      statusSnapshot: null,
      createdAt: new Date().toISOString()
    };

    if (!mockEvaluations[ideaId]) {
      mockEvaluations[ideaId] = [];
    }
    mockEvaluations[ideaId].push(evaluation);
    idea.evaluationCount += 1;

    return HttpResponse.json(evaluation, { status: 201 });
  }),

  // GET /api/v1/ideas/:id/evaluations - Get evaluation history
  http.get(`${API_BASE}/ideas/:id/evaluations`, ({ request, params }) => {
    const authHeader = request.headers.get('authorization');
    if (!authHeader) {
      return HttpResponse.json(
        { message: 'Authentication required' },
        { status: 401 }
      );
    }

    const ideaId = parseInt(params.id);
    const idea = mockIdeas.find(i => i.id === ideaId);

    if (!idea) {
      return HttpResponse.json(
        { message: `Idea with ID ${ideaId} not found` },
        { status: 404 }
      );
    }

    return HttpResponse.json({
      ideaId: ideaId,
      evaluations: mockEvaluations[ideaId] || []
    });
  }),

  // GET /api/v1/ideas/:ideaId/attachments/:attachmentId - Download attachment
  http.get(`${API_BASE}/ideas/:ideaId/attachments/:attachmentId`, ({ request, params }) => {
    const authHeader = request.headers.get('authorization');
    if (!authHeader) {
      return HttpResponse.json(
        { message: 'Authentication required' },
        { status: 401 }
      );
    }

    const ideaId = parseInt(params.ideaId);
    const idea = mockIdeas.find(i => i.id === ideaId);

    if (!idea || !idea.hasAttachment) {
      return HttpResponse.json(
        { message: 'Attachment not found' },
        { status: 404 }
      );
    }

    // Return text content instead of Blob to avoid Vitest stream errors
    return HttpResponse.text('Mock PDF content', {
      status: 200,
      headers: {
        'Content-Type': 'application/pdf',
        'Content-Disposition': 'attachment; filename="proposal.pdf"'
      }
    });
  })
];
