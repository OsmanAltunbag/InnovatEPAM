import { useCallback, useEffect, useRef, useState } from "react";
import * as ideaService from "../services/ideaService";
import useAuth from "./useAuth";

/**
 * Custom hook for managing ideas state and operations
 * @returns {Object} Ideas state and operation functions
 */
export const useIdeas = ({ autoLoad = true, initialParams = {} } = {}) => {
  const { isSessionReady, isAuthenticated, token } = useAuth();
  const [ideas, setIdeas] = useState([]);
  const [selectedIdea, setSelectedIdea] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [pagination, setPagination] = useState({
    pageNumber: 0,
    pageSize: 10,
    totalElements: 0,
    totalPages: 0
  });
  const initialParamsRef = useRef(initialParams);

  useEffect(() => {
    initialParamsRef.current = initialParams;
  }, [initialParams]);

  /**
   * Fetch paginated list of ideas with optional filters
   */
  const fetchIdeas = useCallback(async (params = {}) => {
    setLoading(true);
    setError(null);
    try {
      const response = await ideaService.getIdeas(params);
      setIdeas(response.content);
      setPagination(response.pageable);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to fetch ideas");
    } finally {
      setLoading(false);
    }
  }, []);

  // Wrap fetchIdeas in useEffect with token/isAuthenticated dependency
  // Do NOT call the API if the token is missing
  useEffect(() => {
    if (!autoLoad || !isSessionReady || !isAuthenticated || !token) {
      return;
    }

    fetchIdeas(initialParamsRef.current);
  }, [autoLoad, fetchIdeas, isSessionReady, isAuthenticated, token]);

  /**
   * Fetch a single idea by ID
   */
  const fetchIdeaById = useCallback(async (ideaId) => {
    setLoading(true);
    setError(null);
    try {
      const idea = await ideaService.getIdeaById(ideaId);
      setSelectedIdea(idea);
      return idea;
    } catch (err) {
      setError(err.response?.data?.message || "Failed to fetch idea");
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * Create a new idea
   */
  const createIdea = useCallback(async (ideaData, file = null) => {
    setLoading(true);
    setError(null);
    try {
      const newIdea = await ideaService.createIdea(ideaData, file);
      setIdeas((prev) => [newIdea, ...prev]);
      return newIdea;
    } catch (err) {
      const errorMessage = err.response?.data?.message || "Failed to create idea";
      setError(errorMessage);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * Update idea status
   */
  const updateStatus = useCallback(async (ideaId, statusData) => {
    setLoading(true);
    setError(null);
    try {
      const updatedIdea = await ideaService.updateIdeaStatus(ideaId, statusData);
      
      // Update in list
      setIdeas((prev) =>
        prev.map((idea) => (idea.id === ideaId ? updatedIdea : idea))
      );
      
      // Update selected idea if it matches
      if (selectedIdea?.id === ideaId) {
        setSelectedIdea(updatedIdea);
      }
      
      return updatedIdea;
    } catch (err) {
      const errorMessage = err.response?.data?.message || "Failed to update status";
      setError(errorMessage);
      throw err;
    } finally {
      setLoading(false);
    }
  }, [selectedIdea]);

  /**
   * Add comment to an idea
   */
  const addComment = useCallback(async (ideaId, comment) => {
    setLoading(true);
    setError(null);
    try {
      const evaluation = await ideaService.addIdeaComment(ideaId, comment);
      
      // Refresh idea details to get updated evaluation count
      if (selectedIdea?.id === ideaId) {
        await fetchIdeaById(ideaId);
      }
      
      return evaluation;
    } catch (err) {
      const errorMessage = err.response?.data?.message || "Failed to add comment";
      setError(errorMessage);
      throw err;
    } finally {
      setLoading(false);
    }
  }, [selectedIdea, fetchIdeaById]);

  /**
   * Get evaluation history for an idea
   */
  const fetchEvaluations = useCallback(async (ideaId) => {
    setLoading(true);
    setError(null);
    try {
      const response = await ideaService.getIdeaEvaluations(ideaId);
      return response.evaluations;
    } catch (err) {
      setError(err.response?.data?.message || "Failed to fetch evaluations");
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * Download attachment
   */
  const downloadAttachment = useCallback(async (ideaId, attachmentId, filename) => {
    try {
      await ideaService.downloadAttachment(ideaId, attachmentId, filename);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to download attachment");
      throw err;
    }
  }, []);

  /**
   * Clear error state
   */
  const clearError = useCallback(() => {
    setError(null);
  }, []);

  /**
   * Clear selected idea
   */
  const clearSelectedIdea = useCallback(() => {
    setSelectedIdea(null);
  }, []);

  return {
    ideas,
    selectedIdea,
    loading,
    error,
    pagination,
    fetchIdeas,
    fetchIdeaById,
    createIdea,
    updateStatus,
    addComment,
    fetchEvaluations,
    downloadAttachment,
    clearError,
    clearSelectedIdea
  };
};
