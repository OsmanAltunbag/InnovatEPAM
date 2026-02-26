import { describe, it, expect } from 'vitest';
import {
  IdeaStatus,
  getStatusLabel,
  getStatusColor,
  getStatusBadgeClass,
  getTimelineDotClass,
  getStatusIcon,
  getAllowedNextStatuses,
  isCommentRequired,
  getStatusSuggestions
} from './statusUtils';

describe('statusUtils', () => {
  describe('IdeaStatus constants', () => {
    it('should have all required status constants', () => {
      expect(IdeaStatus.SUBMITTED).toBe('SUBMITTED');
      expect(IdeaStatus.UNDER_REVIEW).toBe('UNDER_REVIEW');
      expect(IdeaStatus.ACCEPTED).toBe('ACCEPTED');
      expect(IdeaStatus.REJECTED).toBe('REJECTED');
    });
  });

  describe('getStatusLabel', () => {
    it('should return "Submitted" for SUBMITTED status', () => {
      // Arrange & Act
      const label = getStatusLabel(IdeaStatus.SUBMITTED);
      // Assert
      expect(label).toBe('Submitted');
    });

    it('should return "Under Review" for UNDER_REVIEW status', () => {
      // Arrange & Act
      const label = getStatusLabel(IdeaStatus.UNDER_REVIEW);
      // Assert
      expect(label).toBe('Under Review');
    });

    it('should return "Accepted" for ACCEPTED status', () => {
      // Arrange & Act
      const label = getStatusLabel(IdeaStatus.ACCEPTED);
      // Assert
      expect(label).toBe('Accepted');
    });

    it('should return "Rejected" for REJECTED status', () => {
      // Arrange & Act
      const label = getStatusLabel(IdeaStatus.REJECTED);
      // Assert
      expect(label).toBe('Rejected');
    });

    it('should return the status value itself if status is unknown', () => {
      // Arrange & Act
      const label = getStatusLabel('UNKNOWN_STATUS');
      // Assert
      expect(label).toBe('UNKNOWN_STATUS');
    });
  });

  describe('getStatusColor', () => {
    it('should return blue classes for SUBMITTED status', () => {
      // Arrange & Act
      const color = getStatusColor(IdeaStatus.SUBMITTED);
      // Assert
      expect(color).toBe('bg-blue-100 text-blue-800');
    });

    it('should return amber classes for UNDER_REVIEW status', () => {
      // Arrange & Act
      const color = getStatusColor(IdeaStatus.UNDER_REVIEW);
      // Assert
      expect(color).toBe('bg-amber-100 text-amber-800');
    });

    it('should return green classes for ACCEPTED status', () => {
      // Arrange & Act
      const color = getStatusColor(IdeaStatus.ACCEPTED);
      // Assert
      expect(color).toBe('bg-emerald-100 text-emerald-800');
    });

    it('should return red classes for REJECTED status', () => {
      // Arrange & Act
      const color = getStatusColor(IdeaStatus.REJECTED);
      // Assert
      expect(color).toBe('bg-red-100 text-red-800');
    });

    it('should return slate classes for unknown status', () => {
      // Arrange & Act
      const color = getStatusColor('UNKNOWN');
      // Assert
      expect(color).toBe('bg-slate-100 text-slate-800');
    });
  });

  describe('getStatusBadgeClass', () => {
    it('should return blue badge classes for SUBMITTED status', () => {
      // Arrange & Act
      const badge = getStatusBadgeClass(IdeaStatus.SUBMITTED);
      // Assert
      expect(badge).toContain('blue');
      expect(badge).toContain('bg-blue-50');
    });

    it('should return amber badge classes for UNDER_REVIEW status', () => {
      // Arrange & Act
      const badge = getStatusBadgeClass(IdeaStatus.UNDER_REVIEW);
      // Assert
      expect(badge).toContain('amber');
      expect(badge).toContain('bg-amber-50');
    });

    it('should return emerald badge classes for ACCEPTED status', () => {
      // Arrange & Act
      const badge = getStatusBadgeClass(IdeaStatus.ACCEPTED);
      // Assert
      expect(badge).toContain('emerald');
      expect(badge).toContain('bg-emerald-50');
    });

    it('should return red badge classes for REJECTED status', () => {
      // Arrange & Act
      const badge = getStatusBadgeClass(IdeaStatus.REJECTED);
      // Assert
      expect(badge).toContain('red');
      expect(badge).toContain('bg-red-50');
    });

    it('should return slate badge classes for unknown status', () => {
      // Arrange & Act
      const badge = getStatusBadgeClass('UNKNOWN');
      // Assert
      expect(badge).toContain('slate');
    });
  });

  describe('getTimelineDotClass', () => {
    it('should return blue dot class for SUBMITTED status', () => {
      // Arrange & Act
      const dot = getTimelineDotClass(IdeaStatus.SUBMITTED);
      // Assert
      expect(dot).toBe('bg-blue-400');
    });

    it('should return amber dot class for UNDER_REVIEW status', () => {
      // Arrange & Act
      const dot = getTimelineDotClass(IdeaStatus.UNDER_REVIEW);
      // Assert
      expect(dot).toBe('bg-amber-400');
    });

    it('should return green dot class for ACCEPTED status', () => {
      // Arrange & Act
      const dot = getTimelineDotClass(IdeaStatus.ACCEPTED);
      // Assert
      expect(dot).toBe('bg-emerald-400');
    });

    it('should return red dot class for REJECTED status', () => {
      // Arrange & Act
      const dot = getTimelineDotClass(IdeaStatus.REJECTED);
      // Assert
      expect(dot).toBe('bg-red-400');
    });

    it('should return slate dot class for unknown status', () => {
      // Arrange & Act
      const dot = getTimelineDotClass('UNKNOWN');
      // Assert
      expect(dot).toBe('bg-slate-400');
    });
  });

  describe('getStatusIcon', () => {
    it('should return 📝 for SUBMITTED status', () => {
      // Arrange & Act
      const icon = getStatusIcon(IdeaStatus.SUBMITTED);
      // Assert
      expect(icon).toBe('📝');
    });

    it('should return 🔍 for UNDER_REVIEW status', () => {
      // Arrange & Act
      const icon = getStatusIcon(IdeaStatus.UNDER_REVIEW);
      // Assert
      expect(icon).toBe('🔍');
    });

    it('should return ✨ for ACCEPTED status', () => {
      // Arrange & Act
      const icon = getStatusIcon(IdeaStatus.ACCEPTED);
      // Assert
      expect(icon).toBe('✨');
    });

    it('should return ✋ for REJECTED status', () => {
      // Arrange & Act
      const icon = getStatusIcon(IdeaStatus.REJECTED);
      // Assert
      expect(icon).toBe('✋');
    });

    it('should return default icon for unknown status', () => {
      // Arrange & Act
      const icon = getStatusIcon('UNKNOWN');
      // Assert
      expect(icon).toBe('•');
    });
  });

  describe('getAllowedNextStatuses', () => {
    it('should allow UNDER_REVIEW for SUBMITTED status', () => {
      // Arrange & Act
      const allowed = getAllowedNextStatuses(IdeaStatus.SUBMITTED);
      // Assert
      expect(allowed).toContain(IdeaStatus.UNDER_REVIEW);
    });

    it('should allow ACCEPTED and REJECTED for UNDER_REVIEW status', () => {
      // Arrange & Act
      const allowed = getAllowedNextStatuses(IdeaStatus.UNDER_REVIEW);
      // Assert
      expect(allowed).toContain(IdeaStatus.ACCEPTED);
      expect(allowed).toContain(IdeaStatus.REJECTED);
    });

    it('should return empty array for ACCEPTED status (terminal)', () => {
      // Arrange & Act
      const allowed = getAllowedNextStatuses(IdeaStatus.ACCEPTED);
      // Assert
      expect(allowed).toEqual([]);
    });

    it('should return empty array for REJECTED status (terminal)', () => {
      // Arrange & Act
      const allowed = getAllowedNextStatuses(IdeaStatus.REJECTED);
      // Assert
      expect(allowed).toEqual([]);
    });

    it('should return empty array for unknown status', () => {
      // Arrange & Act
      const allowed = getAllowedNextStatuses('UNKNOWN');
      // Assert
      expect(allowed).toEqual([]);
    });
  });

  describe('isCommentRequired', () => {
    it('should return true for REJECTED status', () => {
      // Arrange & Act
      const required = isCommentRequired(IdeaStatus.REJECTED);
      // Assert
      expect(required).toBe(true);
    });

    it('should return true for ACCEPTED status', () => {
      // Arrange & Act
      const required = isCommentRequired(IdeaStatus.ACCEPTED);
      // Assert
      expect(required).toBe(true);
    });

    it('should return false for UNDER_REVIEW status', () => {
      // Arrange & Act
      const required = isCommentRequired(IdeaStatus.UNDER_REVIEW);
      // Assert
      expect(required).toBe(false);
    });

    it('should return false for SUBMITTED status', () => {
      // Arrange & Act
      const required = isCommentRequired(IdeaStatus.SUBMITTED);
      // Assert
      expect(required).toBe(false);
    });

    it('should return false for unknown status', () => {
      // Arrange & Act
      const required = isCommentRequired('UNKNOWN');
      // Assert
      expect(required).toBe(false);
    });
  });

  describe('getStatusSuggestions', () => {
    it('should provide suggestions for SUBMITTED status', () => {
      // Arrange & Act
      const suggestions = getStatusSuggestions(IdeaStatus.SUBMITTED);
      // Assert
      expect(suggestions).toContain(IdeaStatus.UNDER_REVIEW);
    });

    it('should provide suggestions for UNDER_REVIEW status', () => {
      // Arrange & Act
      const suggestions = getStatusSuggestions(IdeaStatus.UNDER_REVIEW);
      // Assert
      expect(Array.isArray(suggestions)).toBe(true);
      expect(suggestions.length).toBeGreaterThan(0);
    });

    it('should return empty array for terminal ACCEPTED status', () => {
      // Arrange & Act
      const suggestions = getStatusSuggestions(IdeaStatus.ACCEPTED);
      // Assert
      expect(suggestions).toEqual([]);
    });

    it('should return empty array for terminal REJECTED status', () => {
      // Arrange & Act
      const suggestions = getStatusSuggestions(IdeaStatus.REJECTED);
      // Assert
      expect(suggestions).toEqual([]);
    });

    it('should return empty array for unknown status', () => {
      // Arrange & Act
      const suggestions = getStatusSuggestions('UNKNOWN');
      // Assert
      expect(suggestions).toEqual([]);
    });
  });
});
