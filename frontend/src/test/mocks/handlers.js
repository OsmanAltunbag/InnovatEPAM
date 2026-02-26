import { http, HttpResponse } from 'msw';
import { ideaHandlers } from './ideaHandlers';

const API_BASE = 'http://localhost:8080/api/auth';

const authHandlers = [
  // Register endpoint
  http.post(`${API_BASE}/register`, async ({ request }) => {
    const body = await request.json();

    // Validate required fields
    if (!body.email || !body.password || !body.role) {
      return HttpResponse.json(
        { message: 'Missing required fields' },
        { status: 400 }
      );
    }

    // Validate email format
    if (!/\S+@\S+\.\S+/.test(body.email)) {
      return HttpResponse.json(
        { message: 'Invalid email format' },
        { status: 400 }
      );
    }

    // Validate password strength
    if (body.password.length < 8) {
      return HttpResponse.json(
        { message: 'Password must be at least 8 characters' },
        { status: 400 }
      );
    }

    // Validate role
    if (!['submitter', 'evaluator/admin'].includes(body.role)) {
      return HttpResponse.json(
        { message: 'Invalid role' },
        { status: 400 }
      );
    }

    // Success response
    return HttpResponse.json(
      {
        message: 'User registered successfully',
        user: {
          id: 'test-id-123',
          email: body.email,
          role: body.role
        }
      },
      { status: 201 }
    );
  }),

  // Login endpoint
  http.post(`${API_BASE}/login`, async ({ request }) => {
    const body = await request.json();

    // Validate required fields
    if (!body.email || !body.password) {
      return HttpResponse.json(
        { message: 'Email and password are required' },
        { status: 400 }
      );
    }

    // Validate email format
    if (!/\S+@\S+\.\S+/.test(body.email)) {
      return HttpResponse.json(
        { message: 'Invalid email or password' },
        { status: 401 }
      );
    }

    // Mock successful login
    const token = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwicm9sZSI6ImV2YWx1YXRvciIsImlhdCI6MTY4MTAwMDAwMH0.test';

    return HttpResponse.json(
      {
        message: 'Login successful',
        token: token,
        user: {
          email: body.email,
          role: 'evaluator'
        }
      },
      { status: 200 }
    );
  }),

  // Current user endpoint
  http.get(`${API_BASE}/me`, ({ request }) => {
    const authHeader = request.headers.get('authorization');

    if (!authHeader) {
      return HttpResponse.json(
        { message: 'Unauthorized' },
        { status: 401 }
      );
    }

    // Mock user data
    return HttpResponse.json(
      {
        id: 'test-id-123',
        email: 'test@example.com',
        role: 'evaluator'
      },
      { status: 200 }
    );
  })
];

// Combine all handlers
export const handlers = [...authHandlers, ...ideaHandlers];
