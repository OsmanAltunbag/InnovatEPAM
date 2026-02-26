/**
 * Maximum file size in bytes (50MB)
 */
export const MAX_FILE_SIZE = 50 * 1024 * 1024;

/**
 * Allowed file types
 */
export const ALLOWED_FILE_TYPES = {
  PDF: "application/pdf",
  PNG: "image/png"
};

/**
 * Allowed file extensions
 */
export const ALLOWED_EXTENSIONS = [".pdf", ".png"];

/**
 * Validate file type by MIME type and extension
 * @param {File} file - File to validate
 * @returns {boolean} True if file type is valid
 */
export const isValidFileType = (file) => {
  if (!file) {
    return false;
  }

  // Check MIME type
  const validMimeTypes = Object.values(ALLOWED_FILE_TYPES);
  if (!validMimeTypes.includes(file.type)) {
    return false;
  }

  // Check extension
  const fileName = file.name.toLowerCase();
  const hasValidExtension = ALLOWED_EXTENSIONS.some((ext) =>
    fileName.endsWith(ext)
  );

  return hasValidExtension;
};

/**
 * Validate file size
 * @param {File} file - File to validate
 * @returns {boolean} True if file size is within limit
 */
export const isValidFileSize = (file) => {
  if (!file) {
    return false;
  }
  return file.size > 0 && file.size <= MAX_FILE_SIZE;
};

/**
 * Validate file (type and size)
 * @param {File} file - File to validate
 * @returns {Object} { valid: boolean, error: string|null }
 */
export const validateFile = (file) => {
  if (!file) {
    return { valid: false, error: "No file selected" };
  }

  if (!isValidFileType(file)) {
    return { valid: false, error: "Only PDF and PNG files are allowed" };
  }

  if (!isValidFileSize(file)) {
    if (file.size === 0) {
      return { valid: false, error: "File is empty" };
    }
    return { valid: false, error: "File size must not exceed 50MB" };
  }

  return { valid: true, error: null };
};

/**
 * Format file size for display
 * @param {number} bytes - File size in bytes
 * @returns {string} Formatted file size (e.g., "2.5 MB")
 */
export const formatFileSize = (bytes) => {
  if (bytes === 0) return "0 Bytes";

  const k = 1024;
  const sizes = ["Bytes", "KB", "MB", "GB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));

  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + " " + sizes[i];
};

/**
 * Get file extension from filename
 * @param {string} filename - File name
 * @returns {string} File extension (e.g., ".pdf")
 */
export const getFileExtension = (filename) => {
  if (!filename) return "";
  const lastDot = filename.lastIndexOf(".");
  if (lastDot === -1) return "";
  return filename.substring(lastDot).toLowerCase();
};

/**
 * Check if filename has allowed extension
 * @param {string} filename - File name
 * @returns {boolean} True if extension is allowed
 */
export const hasAllowedExtension = (filename) => {
  const extension = getFileExtension(filename);
  return ALLOWED_EXTENSIONS.includes(extension);
};
