/**
 * Idea status constants
 */
export const IdeaStatus = {
  SUBMITTED: "SUBMITTED",
  UNDER_REVIEW: "UNDER_REVIEW",
  ACCEPTED: "ACCEPTED",
  REJECTED: "REJECTED"
};

/**
 * Get human-readable label for status
 * @param {string} status - Status value
 * @returns {string} Display label
 */
export const getStatusLabel = (status) => {
  const labels = {
    [IdeaStatus.SUBMITTED]: "Submitted",
    [IdeaStatus.UNDER_REVIEW]: "Under Review",
    [IdeaStatus.ACCEPTED]: "Accepted",
    [IdeaStatus.REJECTED]: "Rejected"
  };
  return labels[status] || status;
};

/**
 * Get Tailwind CSS color classes for status badge
 * @param {string} status - Status value
 * @returns {string} Tailwind CSS classes
 */
export const getStatusColor = (status) => {
  const colors = {
    [IdeaStatus.SUBMITTED]: "bg-blue-100 text-blue-800",
    [IdeaStatus.UNDER_REVIEW]: "bg-yellow-100 text-yellow-800",
    [IdeaStatus.ACCEPTED]: "bg-green-100 text-green-800",
    [IdeaStatus.REJECTED]: "bg-red-100 text-red-800"
  };
  return colors[status] || "bg-gray-100 text-gray-800";
};

/**
 * Get status icon (using emoji or text symbols)
 * @param {string} status - Status value
 * @returns {string} Icon representation
 */
export const getStatusIcon = (status) => {
  const icons = {
    [IdeaStatus.SUBMITTED]: "📝",
    [IdeaStatus.UNDER_REVIEW]: "🔍",
    [IdeaStatus.ACCEPTED]: "✅",
    [IdeaStatus.REJECTED]: "❌"
  };
  return icons[status] || "•";
};

/**
 * Check if status transition is valid
 * @param {string} currentStatus - Current status
 * @param {string} targetStatus - Target status
 * @returns {boolean} True if transition is allowed
 */
export const isValidStatusTransition = (currentStatus, targetStatus) => {
  const validTransitions = {
    [IdeaStatus.SUBMITTED]: [IdeaStatus.UNDER_REVIEW, IdeaStatus.REJECTED],
    [IdeaStatus.UNDER_REVIEW]: [IdeaStatus.ACCEPTED, IdeaStatus.REJECTED],
    [IdeaStatus.ACCEPTED]: [],
    [IdeaStatus.REJECTED]: []
  };

  const allowed = validTransitions[currentStatus] || [];
  return allowed.includes(targetStatus);
};

/**
 * Get list of allowed next statuses
 * @param {string} currentStatus - Current status
 * @returns {Array<string>} Array of allowed status values
 */
export const getAllowedNextStatuses = (currentStatus) => {
  const validTransitions = {
    [IdeaStatus.SUBMITTED]: [IdeaStatus.UNDER_REVIEW, IdeaStatus.REJECTED],
    [IdeaStatus.UNDER_REVIEW]: [IdeaStatus.ACCEPTED, IdeaStatus.REJECTED],
    [IdeaStatus.ACCEPTED]: [],
    [IdeaStatus.REJECTED]: []
  };

  return validTransitions[currentStatus] || [];
};

/**
 * Check if comment is required for status transition
 * @param {string} targetStatus - Target status
 * @returns {boolean} True if comment is mandatory
 */
export const isCommentRequired = (targetStatus) => {
  return targetStatus === IdeaStatus.REJECTED;
};
