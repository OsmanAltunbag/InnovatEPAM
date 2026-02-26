import { useState, useCallback } from "react";
import { validateFile } from "../utils/fileUploadUtils";

/**
 * Custom hook for managing file upload state
 * @returns {Object} File upload state and operation functions
 */
export const useFileUpload = () => {
  const [file, setFile] = useState(null);
  const [fileError, setFileError] = useState(null);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [isUploading, setIsUploading] = useState(false);

  /**
   * Handle file selection with validation
   */
  const handleFileSelect = useCallback((selectedFile) => {
    setFileError(null);
    setUploadProgress(0);

    if (!selectedFile) {
      setFile(null);
      return;
    }

    const validation = validateFile(selectedFile);
    if (!validation.valid) {
      setFileError(validation.error);
      setFile(null);
      return;
    }

    setFile(selectedFile);
  }, []);

  /**
   * Handle file input change event
   */
  const handleFileChange = useCallback((event) => {
    const selectedFile = event.target.files?.[0];
    handleFileSelect(selectedFile);
  }, [handleFileSelect]);

  /**
   * Handle drag and drop file selection
   */
  const handleFileDrop = useCallback((event) => {
    event.preventDefault();
    const droppedFile = event.dataTransfer.files?.[0];
    handleFileSelect(droppedFile);
  }, [handleFileSelect]);

  /**
   * Clear selected file
   */
  const clearFile = useCallback(() => {
    setFile(null);
    setFileError(null);
    setUploadProgress(0);
    setIsUploading(false);
  }, []);

  /**
   * Set upload progress (0-100)
   */
  const setProgress = useCallback((progress) => {
    setUploadProgress(Math.min(100, Math.max(0, progress)));
  }, []);

  /**
   * Mark upload as started
   */
  const startUpload = useCallback(() => {
    setIsUploading(true);
    setUploadProgress(0);
    setFileError(null);
  }, []);

  /**
   * Mark upload as completed
   */
  const completeUpload = useCallback(() => {
    setIsUploading(false);
    setUploadProgress(100);
  }, []);

  /**
   * Mark upload as failed
   */
  const failUpload = useCallback((error) => {
    setIsUploading(false);
    setFileError(error || "Upload failed");
    setUploadProgress(0);
  }, []);

  /**
   * Reset upload state
   */
  const resetUpload = useCallback(() => {
    setFile(null);
    setFileError(null);
    setUploadProgress(0);
    setIsUploading(false);
  }, []);

  /**
   * Check if file is valid for upload
   */
  const isValid = file !== null && fileError === null;

  return {
    file,
    fileError,
    uploadProgress,
    isUploading,
    isValid,
    handleFileSelect,
    handleFileChange,
    handleFileDrop,
    clearFile,
    setProgress,
    startUpload,
    completeUpload,
    failUpload,
    resetUpload
  };
};
