import api from "./api";

/**
 * Create a new idea with optional file attachment
 * @param {Object} ideaData - { title, description, category }
 * @param {File|null} file - Optional PDF or PNG file
 * @returns {Promise<Object>} Created idea response
 */
export const createIdea = async (ideaData, file = null) => {
  const formData = new FormData();
  formData.append("title", ideaData.title);
  formData.append("description", ideaData.description);
  formData.append("category", ideaData.category);
  
  if (file) {
    formData.append("file", file);
  }

  const response = await api.post("/ideas", formData, {
    headers: {
      "Content-Type": "multipart/form-data"
    }
  });
  return response.data;
};

/**
 * Get paginated list of ideas with optional filters
 * @param {Object} params - { page, size, status, category }
 * @returns {Promise<Object>} Paginated ideas response
 */
export const getIdeas = async (params = {}) => {
  const { page = 0, size = 10, status, category } = params;
  const queryParams = new URLSearchParams({
    page: page.toString(),
    size: size.toString()
  });

  if (status) {
    queryParams.append("status", status);
  }
  if (category) {
    queryParams.append("category", category);
  }

  const response = await api.get(`/ideas?${queryParams.toString()}`);
  return response.data;
};

/**
 * Get detailed information about a specific idea
 * @param {number} ideaId - Idea ID
 * @returns {Promise<Object>} Idea detail response
 */
export const getIdeaById = async (ideaId) => {
  const response = await api.get(`/ideas/${ideaId}`);
  return response.data;
};

/**
 * Update the status of an idea (evaluator/admin only)
 * @param {number} ideaId - Idea ID
 * @param {Object} statusData - { newStatus, comment }
 * @returns {Promise<Object>} Updated idea response
 */
export const updateIdeaStatus = async (ideaId, statusData) => {
  const response = await api.patch(`/ideas/${ideaId}/status`, statusData);
  return response.data;
};

/**
 * Add a comment to an idea (evaluator/admin only)
 * @param {number} ideaId - Idea ID
 * @param {string} comment - Comment text
 * @returns {Promise<Object>} Created evaluation response
 */
export const addIdeaComment = async (ideaId, comment) => {
  const response = await api.post(`/ideas/${ideaId}/comments`, { comment });
  return response.data;
};

// Alias for backward compatibility
export const addComment = addIdeaComment;

/**
 * Get evaluation history for an idea
 * @param {number} ideaId - Idea ID
 * @returns {Promise<Object>} Evaluation history response
 */
export const getIdeaEvaluations = async (ideaId) => {
  const response = await api.get(`/ideas/${ideaId}/evaluations`);
  return response.data;
};

/**
 * Download an idea attachment
 * @param {number} ideaId - Idea ID
 * @param {number} attachmentId - Attachment ID
 * @param {string} filename - Original filename for download
 * @returns {Promise<void>}
 */
export const downloadAttachment = async (ideaId, attachmentId, filename) => {
  const response = await api.get(`/ideas/${ideaId}/attachments/${attachmentId}`, {
    responseType: "blob"
  });
  
  // response.data is already a Blob, use it directly
  const url = window.URL.createObjectURL(response.data);
  const link = document.createElement("a");
  link.href = url;
  link.setAttribute("download", filename || "attachment");
  document.body.appendChild(link);
  link.click();
  link.parentNode.removeChild(link);
  window.URL.revokeObjectURL(url);
};
