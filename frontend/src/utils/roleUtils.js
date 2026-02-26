/**
 * Role utility functions for consistent role checking across the application.
 * 
 * Handles flexible role name matching:
 * - Case-insensitive comparisons
 * - Combined roles like "evaluator/admin" 
 * - Backward compatibility with different role formats
 */

/**
 * Normalize a role string for comparison
 * - Convert to lowercase
 * - Trim whitespace
 */
const normalizeRole = (role) => {
  if (!role || typeof role !== 'string') {
    return '';
  }
  return role.trim().toLowerCase();
};

/**
 * Check if user has admin role
 * @param {string} userRole - The user's role from JWT
 * @returns {boolean}
 */
export const isAdmin = (userRole) => {
  const normalized = normalizeRole(userRole);
  // Matches: "admin", "ADMIN", "Admin", "evaluator/admin", etc.
  return normalized.includes('admin');
};

/**
 * Check if user has evaluator role
 * @param {string} userRole - The user's role from JWT
 * @returns {boolean}
 */
export const isEvaluator = (userRole) => {
  const normalized = normalizeRole(userRole);
  // Matches: "evaluator", "EVALUATOR", "Evaluator", "evaluator/admin", etc.
  return normalized.includes('evaluator');
};

/**
 * Check if user has submitter role
 * @param {string} userRole - The user's role from JWT
 * @returns {boolean}
 */
export const isSubmitter = (userRole) => {
  const normalized = normalizeRole(userRole);
  // Matches: "submitter", "SUBMITTER", "Submitter", etc.
  return normalized === 'submitter';
};

/**
 * Check if user can perform evaluation tasks (evaluator or admin)
 * @param {string} userRole - The user's role from JWT
 * @returns {boolean}
 */
export const canEvaluate = (userRole) => {
  return isEvaluator(userRole) || isAdmin(userRole);
};

/**
 * Check if user can submit ideas (submitter or admin)
 * @param {string} userRole - The user's role from JWT
 * @returns {boolean}
 */
export const canSubmit = (userRole) => {
  return isSubmitter(userRole) || isAdmin(userRole);
};

/**
 * Get human-readable role label from raw role name
 * @param {string} userRole - The user's role from JWT
 * @returns {string} - Human-readable label
 */
export const getRoleLabel = (userRole) => {
  if (!userRole) return 'Unknown';
  
  const normalized = normalizeRole(userRole);
  
  if (normalized.includes('evaluator') && normalized.includes('admin')) {
    return 'Admin (Evaluator)';
  }
  if (normalized.includes('evaluator')) {
    return 'Evaluator';
  }
  if (normalized.includes('admin')) {
    return 'Admin';
  }
  if (normalized === 'submitter') {
    return 'Submitter';
  }
  
  // Default: capitalize first letter
  return userRole.charAt(0).toUpperCase() + userRole.slice(1).toLowerCase();
};
