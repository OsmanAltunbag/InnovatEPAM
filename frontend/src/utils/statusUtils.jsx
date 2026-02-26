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
 * Get Tailwind CSS color classes for status badge (primary)
 * @param {string} status - Status value
 * @returns {string} Tailwind CSS classes for badge background and text
 */
export const getStatusColor = (status) => {
  const colors = {
    [IdeaStatus.SUBMITTED]: "bg-blue-100 text-blue-800",
    [IdeaStatus.UNDER_REVIEW]: "bg-amber-100 text-amber-800",
    [IdeaStatus.ACCEPTED]: "bg-emerald-100 text-emerald-800",
    [IdeaStatus.REJECTED]: "bg-red-100 text-red-800"
  };
  return colors[status] || "bg-slate-100 text-slate-800";
};

/**
 * Get modern badge styling with border
 * @param {string} status - Status value
 * @returns {string} Tailwind CSS classes for modern badge
 */
export const getStatusBadgeClass = (status) => {
  const badges = {
    [IdeaStatus.SUBMITTED]: "bg-blue-50 border-blue-200 text-blue-700",
    [IdeaStatus.UNDER_REVIEW]: "bg-amber-50 border-amber-200 text-amber-700",
    [IdeaStatus.ACCEPTED]: "bg-emerald-50 border-emerald-200 text-emerald-700",
    [IdeaStatus.REJECTED]: "bg-red-50 border-red-200 text-red-700"
  };
  return badges[status] || "bg-slate-50 border-slate-200 text-slate-700";
};

/**
 * Get timeline dot styling
 * @param {string} status - Status value
 * @returns {string} Tailwind CSS classes for timeline dot
 */
export const getTimelineDotClass = (status) => {
  const dots = {
    [IdeaStatus.SUBMITTED]: "bg-blue-400",
    [IdeaStatus.UNDER_REVIEW]: "bg-amber-400",
    [IdeaStatus.ACCEPTED]: "bg-emerald-400",
    [IdeaStatus.REJECTED]: "bg-red-400"
  };
  return dots[status] || "bg-slate-400";
};

/**
 * Get status icon (using emoji)
 * @param {string} status - Status value
 * @returns {string} Icon representation
 */
export const getStatusIcon = (status) => {
  const icons = {
    [IdeaStatus.SUBMITTED]: "📝",
    [IdeaStatus.UNDER_REVIEW]: "🔍",
    [IdeaStatus.ACCEPTED]: "✨",
    [IdeaStatus.REJECTED]: "✋"
  };
  return icons[status] || "•";
};

/**
 * Get SVG icon for status
 * @param {string} status - Status value
 * @returns {JSX.Element} SVG icon component
 */
export const getStatusSVGIcon = (status) => {
  const baseClass = "w-4 h-4";
  
  const icons = {
    [IdeaStatus.SUBMITTED]: (
      <svg className={baseClass} fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
      </svg>
    ),
    [IdeaStatus.UNDER_REVIEW]: (
      <svg className={baseClass} fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
      </svg>
    ),
    [IdeaStatus.ACCEPTED]: (
      <svg className={baseClass} fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
    ),
    [IdeaStatus.REJECTED]: (
      <svg className={baseClass} fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16H5v-4m0 0V8m0 4H3m2 5h8m4-11h.01M19 3H5a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2V5a2 2 0 00-2-2z" />
      </svg>
    )
  };
  
  return icons[status] || null;
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

/**
 * Get next available status suggestions
 * @param {string} currentStatus - Current status
 * @returns {Array<Object>} Array of status objects with label and description
 */
export const getStatusSuggestions = (currentStatus) => {
  const suggestions = {
    [IdeaStatus.SUBMITTED]: [
      {
        value: IdeaStatus.UNDER_REVIEW,
        label: "Start Review",
        description: "Begin evaluating this idea"
      },
      {
        value: IdeaStatus.REJECTED,
        label: "Reject",
        description: "Decline with feedback required"
      }
    ],
    [IdeaStatus.UNDER_REVIEW]: [
      {
        value: IdeaStatus.ACCEPTED,
        label: "Accept",
        description: "Approve for implementation"
      },
      {
        value: IdeaStatus.REJECTED,
        label: "Reject",
        description: "Decline with feedback"
      }
    ],
    [IdeaStatus.ACCEPTED]: [],
    [IdeaStatus.REJECTED]: []
  };

  return suggestions[currentStatus] || [];
};
