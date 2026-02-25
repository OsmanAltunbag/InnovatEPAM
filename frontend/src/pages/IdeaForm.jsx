import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useIdeas } from "../hooks/useIdeas";
import { useFileUpload } from "../hooks/useFileUpload";
import FileUpload from "../components/FileUpload";

export default function IdeaForm() {
  const navigate = useNavigate();
  const { createIdea, loading, error } = useIdeas();
  const { 
    file, 
    fileError, 
    handleFileSelect, 
    clearFile 
  } = useFileUpload();

  const [formData, setFormData] = useState({
    title: "",
    description: "",
    category: ""
  });
  const [formErrors, setFormErrors] = useState({});

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    // Clear error when user starts typing
    if (formErrors[name]) {
      setFormErrors(prev => ({ ...prev, [name]: "" }));
    }
  };

  const validate = () => {
    const errors = {};
    if (!formData.title.trim()) {
      errors.title = "Title is required";
    } else if (formData.title.length > 255) {
      errors.title = "Title must not exceed 255 characters";
    }
    if (!formData.description.trim()) {
      errors.description = "Description is required";
    }
    if (!formData.category.trim()) {
      errors.category = "Category is required";
    } else if (formData.category.length > 50) {
      errors.category = "Category must not exceed 50 characters";
    }
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validate()) {
      return;
    }

    if (fileError) {
      return;
    }

    try {
      await createIdea(formData, file);
      navigate("/ideas");
    } catch (err) {
      // Error is handled by the hook
    }
  };

  return (
    <div className="mx-auto max-w-3xl">
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-slate-900">Submit New Idea</h1>
        <p className="mt-2 text-slate-600">
          Share your innovative ideas to improve our organization
        </p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Main Card */}
        <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
          <div className="space-y-5">
            {/* Title */}
            <div>
              <label htmlFor="title" className="block text-sm font-medium text-slate-700">
                Title<span className="text-red-500">*</span>
              </label>
              <input
                id="title"
                name="title"
                type="text"
                value={formData.title}
                onChange={handleChange}
                placeholder="Enter a descriptive title"
                className="mt-1.5 w-full rounded-lg border border-slate-300 px-4 py-2.5 text-slate-900 placeholder-slate-400 transition-colors focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
              />
              {formErrors.title && (
                <p className="mt-1.5 text-sm text-red-600">{formErrors.title}</p>
              )}
            </div>

            {/* Category */}
            <div>
              <label htmlFor="category" className="block text-sm font-medium text-slate-700">
                Category<span className="text-red-500">*</span>
              </label>
              <input
                id="category"
                name="category"
                type="text"
                value={formData.category}
                onChange={handleChange}
                placeholder="e.g., Technology, HR, Sustainability"
                className="mt-1.5 w-full rounded-lg border border-slate-300 px-4 py-2.5 text-slate-900 placeholder-slate-400 transition-colors focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
              />
              {formErrors.category && (
                <p className="mt-1.5 text-sm text-red-600">{formErrors.category}</p>
              )}
            </div>

            {/* Description */}
            <div>
              <label htmlFor="description" className="block text-sm font-medium text-slate-700">
                Description<span className="text-red-500">*</span>
              </label>
              <textarea
                id="description"
                name="description"
                rows={6}
                value={formData.description}
                onChange={handleChange}
                placeholder="Provide detailed information about your idea, expected outcomes, and implementation approach..."
                className="mt-1.5 w-full rounded-lg border border-slate-300 px-4 py-2.5 text-slate-900 placeholder-slate-400 transition-colors focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
              />
              {formErrors.description && (
                <p className="mt-1.5 text-sm text-red-600">{formErrors.description}</p>
              )}
            </div>

            {/* File Upload */}
            <FileUpload
              file={file}
              onFileSelect={handleFileSelect}
              onFileClear={clearFile}
              error={fileError}
            />
          </div>
        </div>

        {/* Error Message */}
        {error && (
          <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3">
            <p className="text-sm text-red-800">{error}</p>
          </div>
        )}

        {/* Action Buttons */}
        <div className="flex items-center gap-3">
          <button
            type="submit"
            disabled={loading}
            className="rounded-lg bg-blue-600 px-6 py-2.5 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {loading ? "Submitting..." : "Submit Idea"}
          </button>
          <button
            type="button"
            onClick={() => navigate("/ideas")}
            className="rounded-lg border border-slate-300 bg-white px-6 py-2.5 text-sm font-semibold text-slate-700 transition-colors hover:bg-slate-50 focus:outline-none focus:ring-2 focus:ring-slate-500 focus:ring-offset-2"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}
