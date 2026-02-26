import { describe, it, expect } from 'vitest';
import { normalizeEmail, isValidEmail, isStrongPassword } from './validators';

describe('validators', () => {
  describe('normalizeEmail', () => {
    it('should convert email to lowercase', () => {
      expect(normalizeEmail('TEST@EXAMPLE.COM')).toBe('test@example.com');
      expect(normalizeEmail('Test@Example.Com')).toBe('test@example.com');
    });

    it('should trim whitespace', () => {
      expect(normalizeEmail('  test@example.com  ')).toBe('test@example.com');
      expect(normalizeEmail('\ttest@example.com\n')).toBe('test@example.com');
    });

    it('should handle already normalized emails', () => {
      expect(normalizeEmail('test@example.com')).toBe('test@example.com');
    });
  });

  describe('isValidEmail', () => {
    it('should accept valid email addresses', () => {
      expect(isValidEmail('test@example.com')).toBe(true);
      expect(isValidEmail('user.name@example.com')).toBe(true);
      expect(isValidEmail('user+tag@example.co.uk')).toBe(true);
      expect(isValidEmail('123@example.com')).toBe(true);
      expect(isValidEmail('test_user@example-domain.com')).toBe(true);
    });

    it('should reject invalid email addresses', () => {
      expect(isValidEmail('invalid-email')).toBe(false);
      expect(isValidEmail('missing@.com')).toBe(false);
      expect(isValidEmail('@example.com')).toBe(false);
      expect(isValidEmail('test@')).toBe(false);
      expect(isValidEmail('test@example')).toBe(false);
      expect(isValidEmail('')).toBe(false);
      expect(isValidEmail('   ')).toBe(false);
    });

    it('should normalize before validation', () => {
      expect(isValidEmail('  TEST@EXAMPLE.COM  ')).toBe(true);
      expect(isValidEmail('User@Example.Com')).toBe(true);
    });

    it('should reject emails with spaces', () => {
      expect(isValidEmail('test user@example.com')).toBe(false);
      expect(isValidEmail('test@example .com')).toBe(false);
    });

    it('should reject emails missing domain extension', () => {
      expect(isValidEmail('test@example')).toBe(false);
      expect(isValidEmail('test@localhost')).toBe(false);
    });
  });

  describe('isStrongPassword', () => {
    it('should accept passwords with 8+ characters', () => {
      expect(isStrongPassword('12345678')).toBe(true);
      expect(isStrongPassword('password')).toBe(true);
      expect(isStrongPassword('Test1234')).toBe(true);
      expect(isStrongPassword('VeryLongPassword123')).toBe(true);
    });

    it('should reject passwords shorter than 8 characters', () => {
      expect(isStrongPassword('1234567')).toBe(false);
      expect(isStrongPassword('short')).toBe(false);
      expect(isStrongPassword('Test123')).toBe(false);
      expect(isStrongPassword('a')).toBe(false);
      expect(isStrongPassword('')).toBe(false);
    });

    it('should reject null or undefined', () => {
      expect(isStrongPassword(null)).toBe(false);
      expect(isStrongPassword(undefined)).toBe(false);
    });

    it('should trim whitespace before checking length', () => {
      expect(isStrongPassword('  12345678  ')).toBe(true);
      expect(isStrongPassword('  short  ')).toBe(false);
      expect(isStrongPassword('       ')).toBe(false); // Only spaces
    });

    it('should accept passwords with special characters', () => {
      expect(isStrongPassword('P@ssw0rd!')).toBe(true);
      expect(isStrongPassword('Test#123$')).toBe(true);
      expect(isStrongPassword('!@#$%^&*()')).toBe(true);
    });
  });
});
