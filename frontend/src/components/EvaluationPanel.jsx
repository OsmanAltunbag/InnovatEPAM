import React, { useState, useEffect } from "react";
import useAuth from "../hooks/useAuth";
import { useIdeas } from "../hooks/useIdeas";
import {
  getAllowedNextStatuses,
  getStatusLabel,
  getStatusIcon,
  getStatusColor,
  getStatusBadgeClass,
  getTimelineDotClass,
  isCommentRequired,
  getStatusSuggestions
} from "../utils/statusUtils";
import { canEvaluate } from "../utils/roleUtils";

/**
 * Modern Evaluation Panel component for premium SaaS dashboard
 * Displays evaluation history timeline and status update form
 */
export default function EvaluationPanel({ idea }) {
  const { user } = useAuth();
  const { updateStatus, addComment, fetchEvaluations, loading } = useIdeas({
    autoLoad: false
  });
  const [evaluations, setEvaluations] = useState([]);
  const [showStatusForm, setShowStatusForm] = useState(false);
  const [showCommentForm, setShowCommentForm] = useState(false);
  const [statusFormData, setStatusFormData] = useState({
    newStatus: "",
    comment: ""
  });
  const [commentText, setCommentText] = useState("");
  const [error, setError] = useState("");
  const [evaluationsLoading, setEvaluationsLoading] = useState(true);

  const isEvaluator = canEvaluate(user?.role);
  const allowedStatuses = getAllowedNextStatuses(idea?.status);
  const statusSuggestions = getStatusSuggestions(idea?.status);

  // Fetch evaluations on component mount or idea change
  useEffect(() => {
    loadEvaluations();
  }, [idea?.id]);

  const loadEvaluations = async () => {
    try {
      setEvaluationsLoading(true);
      const data = await fetchEvaluations(idea?.id);
      setEvaluations(data || []);
    } catch (err) {
      // Error handled by hook, show empty state
      setEvaluations([]);
    } finally {
      setEvaluationsLoading(false);
    }
  };

  const handleStatusSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (!statusFormData.newStatus) {
      setError("Please select a status");
      return;
    }

    if (
      isCommentRequired(statusFormData.newStatus) &&
      !statusFormData.comment.trim()
    ) {
      setError("Comment is required when rejecting an idea");
      return;
    }

    try {
      await updateStatus(idea.id, statusFormData);
      await loadEvaluations();
      setShowStatusForm(false);
      setStatusFormData({ newStatus: "", comment: "" });
    } catch (err) {
      setError(err.response?.data?.message || "Failed to update status");
    }
  };

  const handleCommentSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (!commentText.trim()) {
      setError("Comment cannot be empty");
      return;
    }

    if (commentText.length > 5000) {
      setError("Comment exceeds 5000 character limit");
      return;
    }

    try {
      await addComment(idea.id, commentText);
      await loadEvaluations();
      setShowCommentForm(false);
      setCommentText("");
    } catch (err) {
      setError(err.response?.data?.message || "Failed to add comment");
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 60) {
      return `${diffMins}m ago`;
    }
    if (diffHours < 24) {
      return `${diffHours}h ago`;
    }
    if (diffDays < 7) {
      return `${diffDays}d ago`;
    }

    return date.toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      year: date.getFullYear() !== now.getFullYear() ? "numeric" : undefined
    });
  };

  const formatFullDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString("en-US", {
      month: "long",
      day: "numeric",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit"
    });
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="border-b border-slate-200 pb-4">
        <h2 className="text-lg font-semibold text-slate-900">
          Evaluation Workflow
        </h2>
        <p className="mt-1 text-sm text-slate-600">
          {isEvaluator ? "Evaluator view" : "Submitter view"}
        </p>
      </div>

      {/* Action Buttons - Only for Evaluators with Allowed Transitions */}
      {isEvaluator && allowedStatuses.length > 0 && (
        <div className="space-y-3">
          {!showStatusForm && !showCommentForm && (
            <div className="grid grid-cols-2 gap-2">
              <button
                onClick={() => setShowStatusForm(true)}
                className="flex items-center justify-center gap-2 rounded-lg bg-gradient-to-r from-blue-600 to-blue-700 px-3 py-2.5 text-sm font-semibold text-white shadow-sm transition-all hover:shadow-md hover:from-blue-700 hover:to-blue-800 active:scale-95"
              >
                <svg
                  className="h-4 w-4"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                  />
                </svg>
                Update Status
              </button>
              <button
                onClick={() => setShowCommentForm(true)}
                className="flex items-center justify-center gap-2 rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm font-semibold text-slate-700 shadow-sm transition-all hover:bg-slate-50 active:scale-95"
              >
                <svg
                  className="h-4 w-4"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z"
                  />
                </svg>
                Comment
              </button>
            </div>
          )}

          {/* Status Update Form - Modern Premium Design */}
          {showStatusForm && (
            <div className="animate-in fade-in slide-in-from-top-2 rounded-xl border border-slate-200 bg-gradient-to-br from-slate-50 to-white p-4 shadow-sm">
              <form onSubmit={handleStatusSubmit} className="space-y-4">
                <div>
                  <label className="block text-sm font-semibold text-slate-900">
                    Update Status
                    <span className="text-red-500">*</span>
                  </label>
                  <p className="mt-1 text-xs text-slate-500">
                    Select the next status for this idea
                  </p>

                  {/* Status Suggestions */}
                  <div className="mt-3 grid grid-cols-1 gap-2">
                    {statusSuggestions.map((suggestion) => (
                      <label
                        key={suggestion.value}
                        className="relative flex cursor-pointer items-start gap-3 rounded-lg border border-slate-200 p-3 transition-all hover:border-blue-400 hover:bg-blue-50"
                      >
                        <input
                          type="radio"
                          name="newStatus"
                          value={suggestion.value}
                          checked={statusFormData.newStatus === suggestion.value}
                          onChange={(e) =>
                            setStatusFormData((prev) => ({
                              ...prev,
                              newStatus: e.target.value
                            }))
                          }
                          className="mt-1 h-4 w-4 text-blue-600 focus:ring-2 focus:ring-blue-500"
                        />
                        <div>
                          <div className="text-sm font-medium text-slate-900">
                            {getStatusIcon(suggestion.value)}{" "}
                            {suggestion.label}
                          </div>
                          <p className="text-xs text-slate-600">
                            {suggestion.description}
                          </p>
                        </div>
                      </label>
                    ))}
                  </div>
                </div>

                {/* Comment Textarea with modern focus ring */}
                <div>
                  <label className="block text-sm font-semibold text-slate-900">
                    Feedback
                    {isCommentRequired(statusFormData.newStatus) && (
                      <span className="text-red-500">*</span>
                    )}
                  </label>
                  <p className="mt-1 text-xs text-slate-500">
                    Share your reasoning and feedback
                  </p>
                  <textarea
                    value={statusFormData.comment}
                    onChange={(e) =>
                      setStatusFormData((prev) => ({
                        ...prev,
                        comment: e.target.value
                      }))
                    }
                    maxLength={5000}
                    rows={4}
                    placeholder="Provide detailed feedback for the submitter..."
                    className="mt-2 w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm bg-white transition-all placeholder:text-slate-400 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 focus:outline-none"
                  />
                  <div className="mt-1.5 flex items-center justify-between">
                    <p className="text-xs text-slate-500">
                      {statusFormData.comment.length}/5000 characters
                    </p>
                    {statusFormData.comment.length > 4500 && (
                      <p className="text-xs text-amber-600">
                        Approaching limit
                      </p>
                    )}
                  </div>
                </div>

                {/* Error Message */}
                {error && (
                  <div className="rounded-lg bg-red-50 border border-red-200 p-3">
                    <p className="text-sm text-red-700">{error}</p>
                  </div>
                )}

                {/* Submit Buttons */}
                <div className="flex gap-2">
                  <button
                    type="submit"
                    disabled={loading || !statusFormData.newStatus}
                    className="flex-1 flex items-center justify-center gap-2 rounded-lg bg-gradient-to-r from-blue-600 to-blue-700 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition-all hover:shadow-md hover:from-blue-700 hover:to-blue-800 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {loading ? (
                      <>
                        <div className="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent" />
                        Updating...
                      </>
                    ) : (
                      <>
                        <svg
                          className="h-4 w-4"
                          fill="none"
                          stroke="currentColor"
                          viewBox="0 0 24 24"
                        >
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={2}
                            d="M5 13l4 4L19 7"
                          />
                        </svg>
                        Update
                      </>
                    )}
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      setShowStatusForm(false);
                      setStatusFormData({ newStatus: "", comment: "" });
                      setError("");
                    }}
                    className="flex-1 rounded-lg border border-slate-300 bg-white px-4 py-2.5 text-sm font-semibold text-slate-700 shadow-sm transition-all hover:bg-slate-50"
                  >
                    Cancel
                  </button>
                </div>
              </form>
            </div>
          )}

          {/* Comment Form - Modern Premium Design */}
          {showCommentForm && (
            <div className="animate-in fade-in slide-in-from-top-2 rounded-xl border border-slate-200 bg-gradient-to-br from-slate-50 to-white p-4 shadow-sm">
              <form onSubmit={handleCommentSubmit} className="space-y-4">
                <div>
                  <label className="block text-sm font-semibold text-slate-900">
                    Add Comment
                    <span className="text-red-500">*</span>
                  </label>
                  <p className="mt-1 text-xs text-slate-500">
                    Your feedback without changing status
                  </p>
                  <textarea
                    value={commentText}
                    onChange={(e) => setCommentText(e.target.value)}
                    maxLength={5000}
                    rows={4}
                    placeholder="Share your thoughts and feedback..."
                    className="mt-2 w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm bg-white transition-all placeholder:text-slate-400 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 focus:outline-none"
                  />
                  <div className="mt-1.5 flex items-center justify-between">
                    <p className="text-xs text-slate-500">
                      {commentText.length}/5000 characters
                    </p>
                    {commentText.length > 4500 && (
                      <p className="text-xs text-amber-600">
                        Approaching limit
                      </p>
                    )}
                  </div>
                </div>

                {/* Error Message */}
                {error && (
                  <div className="rounded-lg bg-red-50 border border-red-200 p-3">
                    <p className="text-sm text-red-700">{error}</p>
                  </div>
                )}

                {/* Submit Buttons */}
                <div className="flex gap-2">
                  <button
                    type="submit"
                    disabled={loading || !commentText.trim()}
                    className="flex-1 flex items-center justify-center gap-2 rounded-lg bg-gradient-to-r from-blue-600 to-blue-700 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition-all hover:shadow-md hover:from-blue-700 hover:to-blue-800 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {loading ? (
                      <>
                        <div className="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent" />
                        Adding...
                      </>
                    ) : (
                      <>
                        <svg
                          className="h-4 w-4"
                          fill="none"
                          stroke="currentColor"
                          viewBox="0 0 24 24"
                        >
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={2}
                            d="M12 4v16m8-8H4"
                          />
                        </svg>
                        Post Comment
                      </>
                    )}
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      setShowCommentForm(false);
                      setCommentText("");
                      setError("");
                    }}
                    className="flex-1 rounded-lg border border-slate-300 bg-white px-4 py-2.5 text-sm font-semibold text-slate-700 shadow-sm transition-all hover:bg-slate-50"
                  >
                    Cancel
                  </button>
                </div>
              </form>
            </div>
          )}
        </div>
      )}

      {/* Vertical Timeline - Premium Dashboard Style */}
      <div className="border-t border-slate-200 pt-6">
        <h3 className="text-sm font-semibold text-slate-900 mb-4">
          Evaluation History
        </h3>

        {evaluationsLoading ? (
          <div className="flex justify-center py-8">
            <div className="space-y-3 w-full">
              {[1, 2].map((i) => (
                <div key={i} className="animate-pulse">
                  <div className="flex gap-3">
                    <div className="h-8 w-8 rounded-full bg-slate-200" />
                    <div className="flex-1 space-y-2">
                      <div className="h-4 bg-slate-200 rounded w-1/3" />
                      <div className="h-3 bg-slate-200 rounded w-full" />
                      <div className="h-3 bg-slate-200 rounded w-5/6" />
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        ) : evaluations.length === 0 ? (
          <div className="rounded-lg border border-slate-200 border-dashed bg-slate-50 px-4 py-8 text-center">
            <svg
              className="mx-auto h-12 w-12 text-slate-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={1.5}
                d="M13 16h-1v-4h-1m1 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
            <p className="mt-2 text-sm text-slate-600">
              {isEvaluator ? "No evaluations yet. Be the first to review!" : "No evaluations yet"}
            </p>
          </div>
        ) : (
          <div className="relative space-y-4">
            {/* Timeline connector line */}
            <div className="absolute left-4 top-8 bottom-0 w-0.5 bg-gradient-to-b from-blue-200 to-slate-200" />

            {evaluations.map((evaluation, index) => (
              <div
                key={evaluation.id}
                className="relative pl-12 group"
              >
                {/* Timeline Dot */}
                <div
                  className={`absolute left-0 top-0.5 h-9 w-9 rounded-full border-4 border-white shadow-sm ring-2 ring-slate-200 flex items-center justify-center transition-all group-hover:ring-4 group-hover:ring-blue-200 ${
                    evaluation.statusSnapshot
                      ? getTimelineDotClass(evaluation.statusSnapshot)
                      : "bg-blue-400"
                  }`}
                >
                  {evaluation.statusSnapshot ? (
                    <span className="text-base">
                      {evaluation.statusSnapshot === "ACCEPTED"
                        ? "✨"
                        : evaluation.statusSnapshot === "REJECTED"
                          ? "✋"
                          : "🔍"}
                    </span>
                  ) : (
                    <span className="text-base">💬</span>
                  )}
                </div>

                {/* Timeline Card */}
                <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm transition-all hover:shadow-md hover:border-blue-200">
                  {/* Header */}
                  <div className="flex items-start justify-between gap-3 mb-2">
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-sm font-semibold text-slate-900">
                          {evaluation.evaluatorName || "Evaluator"}
                        </span>
                        <span className="text-xs bg-slate-100 text-slate-600 px-2 py-0.5 rounded">
                          {evaluation.evaluatorRole || "Evaluator"}
                        </span>
                      </div>
                      <p className="text-xs text-slate-500 mt-0.5">
                        {formatDate(evaluation.createdAt)}
                      </p>
                    </div>
                  </div>

                  {/* Status Badge if present */}
                  {evaluation.statusSnapshot && (
                    <div className={`inline-block mb-2 px-3 py-1 rounded-full text-xs font-semibold border ${getStatusBadgeClass(
                      evaluation.statusSnapshot
                    )}`}>
                      {getStatusIcon(evaluation.statusSnapshot)} {getStatusLabel(
                        evaluation.statusSnapshot
                      )}
                    </div>
                  )}

                  {/* Comment */}
                  <p className="text-sm text-slate-700 leading-relaxed whitespace-pre-wrap break-words">
                    {evaluation.comment}
                  </p>

                  {/* Timestamp Tooltip on Hover */}
                  <p className="text-xs text-slate-400 mt-3 opacity-0 group-hover:opacity-100 transition-opacity">
                    {formatFullDate(evaluation.createdAt)}
                  </p>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Non-evaluator message */}
      {!isEvaluator && (
        <div className="rounded-lg border border-blue-200 bg-blue-50 p-3">
          <p className="text-xs text-blue-700">
            💡 Only evaluators and admin users can update status and add assessments.
          </p>
        </div>
      )}
    </div>
  );
}
