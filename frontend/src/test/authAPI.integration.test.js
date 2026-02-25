import { describe, it, expect } from 'vitest';
import { HttpResponse, http } from 'msw';
import { server } from './mocks/server';

const API_BASE = 'http://localhost:8080/api/auth';

describe('Auth API - Integration Tests', () => {

  describe('Register endpoint', () => {
    it('should handle registration with valid data', async () => {
      const response = await fetch(`${API_BASE}/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: 'newuser@example.com',
          password: 'SecurePassword123',
          role: 'submitter'
        })
      });

      expect(response.status).toBe(201);
      const data = await response.json();
      expect(data.user).toBeDefined();
      expect(data.user.email).toBe('newuser@example.com');
    });

    it('should reject registration with invalid email', async () => {
      const response = await fetch(`${API_BASE}/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: 'invalid-email',
          password: 'SecurePassword123',
          role: 'submitter'
        })
      });

      expect(response.status).toBe(400);
    });

    it('should reject registration with weak password', async () => {
      const response = await fetch(`${API_BASE}/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: 'user@example.com',
          password: 'weak',
          role: 'submitter'
        })
      });

      expect(response.status).toBe(400);
    });

    it('should reject registration with missing fields', async () => {
      const response = await fetch(`${API_BASE}/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: 'user@example.com'
          // Missing password and role
        })
      });

      expect(response.status).toBe(400);
    });

    it('should reject registration with invalid role', async () => {
      const response = await fetch(`${API_BASE}/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: 'user@example.com',
          password: 'SecurePassword123',
          role: 'invalid-role'
        })
      });

      expect(response.status).toBe(400);
    });
  });

  describe('Login endpoint', () => {
    it('should handle login with valid credentials', async () => {
      const response = await fetch(`${API_BASE}/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: 'test@example.com',
          password: 'password123'
        })
      });

      expect(response.status).toBe(200);
      const data = await response.json();
      expect(data.token).toBeDefined();
      expect(data.user).toBeDefined();
    });

    it('should reject login with invalid email format', async () => {
      const response = await fetch(`${API_BASE}/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: 'invalid-email',
          password: 'password123'
        })
      });

      expect(response.status).toBe(401);
    });

    it('should reject login with missing email', async () => {
      const response = await fetch(`${API_BASE}/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          password: 'password123'
        })
      });

      expect(response.status).toBe(400);
    });

    it('should reject login with missing password', async () => {
      const response = await fetch(`${API_BASE}/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: 'test@example.com'
        })
      });

      expect(response.status).toBe(400);
    });
  });

  describe('Me endpoint', () => {
    it('should return current user when authenticated', async () => {
      const response = await fetch(`${API_BASE}/me`, {
        method: 'GET',
        headers: {
          'Authorization': 'Bearer test-token'
        }
      });

      expect(response.status).toBe(200);
      const data = await response.json();
      expect(data.email).toBeDefined();
      expect(data.role).toBeDefined();
    });

    it('should reject request without authorization header', async () => {
      const response = await fetch(`${API_BASE}/me`, {
        method: 'GET'
      });

      expect(response.status).toBe(401);
    });
  });

  describe('Error handling', () => {
    it('should return 500 on server error', async () => {
      server.use(
        http.post(`${API_BASE}/register`, () => {
          return HttpResponse.json(
            { message: 'Internal server error' },
            { status: 500 }
          );
        })
      );

      const response = await fetch(`${API_BASE}/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: 'user@example.com',
          password: 'SecurePassword123',
          role: 'submitter'
        })
      });

      expect(response.status).toBe(500);
    });

    it('should handle malformed JSON request', async () => {
      const response = await fetch(`${API_BASE}/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: 'invalid json'
      });

      // MSW should handle this or the client should catch it
      expect(response.status !== 200).toBe(true);
    });
  });
});
