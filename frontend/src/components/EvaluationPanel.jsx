import React, { useState, useEffect } from "react";
import useAuth from "../hooks/useAuth";
import { useIdeas } from "../hooks/useIdeas";
import {
  getAllowedNextStatuses,
  getStatusLabel,
  getStatusIcon,
  isCommentRequired
} from "../utils/statusUtils";

export default function EvaluationPanel({ idea }) {
  const { user } = useAuth();
  const { updateStatus, addComment, fetchEvaluations, loading } = useIdeas();
  const [evaluations, setEvaluations] = useState([]);
  const [showStatusForm, setShowStatusForm] = useState(false);
  const [showCommentForm, setShowCommentForm] = useState(false);
  const [statusFormData, setStatusFormData] = useState({
    newStatus: "",
    comment: ""
  });
  const [commentText, setCommentText] = useState("");
  const [error, setError] = useState("");

  const isEvaluator = user?.role === "evaluator" || user?.role === "admin";
  const allowedStatuses = getAllowedNextStatuses(idea.status);

  useEffect(() => {
    loadEvaluations();
  }, [idea.id]);

  const loadEvaluations = async () => {
    try {
      const data = await fetchEvaluations(idea.id);
      setEvaluations(data);
    } catch (err) {
      // Error handled by hook
    }
  };

  const handleStatusSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (!statusFormData.newStatus) {
      setError("Please select a status");
      return;
    }

    if (isCommentRequired(statusFormData.newStatus) && !statusFormData.comment.trim()) {
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
    return date.toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit"
    });
  };

  return (
    <div className="space-y-4">
      <h2 className="text-lg font-semibold text-slate-900">Evaluation</h2>

      {/* Action Buttons */}
      {isEvaluator && allowedStatuses.length > 0 && (
        <div className="space-y-2">
          {!showStatusForm && !showCommentForm && (
            <>
              <button
                onClick={() => setShowStatusForm(true)}
                className="w-full rounded-lg bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-blue-700"
              >
                Update Status
              </button>
              <button
                onClick={() => setShowCommentForm(true)}
                className="w-full rounded-lg border border-slate-300 bg-white px-4 py-2.5 text-sm font-semibold text-slate-700 transition-colors hover:bg-slate-50"
              >
                Add Comment
              </button>
            </>
          )}

          {/* Status Update Form */}
          {showStatusForm && (
            <div className="rounded-lg border border-slate-200 bg-white p-4">
              <form onSubmit={handleStatusSubmit} className="space-y-3">
                <div>
                  <label className="block text-sm font-medium text-slate-700">
                    New Status<span className="text-red-500">*</span>
                  </label>
                  <select
                    value={statusFormData.newStatus}
                    onChange={(e) =>
                      setStatusFormData(prev => ({ ...prev, newStatus: e.target.value }))
                    }
                    className="mt-1.5 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                  >
                    <option value="">Select status...</option>
                    {allowedStatuses.map((status) => (
                      <option key={status} value={status}>
                        {getStatusIcon(status)} {getStatusLabel(status)}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-slate-700">
                    Comment
                    {isCommentRequired(statusFormData.newStatus) && (
                      <span className="text-red-500">*</span>
                    )}
                  </label>
                  <textarea
                    value={statusFormData.comment}
                    onChange={(e) =>
                      setStatusFormData(prev => ({ ...prev, comment: e.target.value }))
                    }
                    rows={3}
                    placeholder="Add your feedback..."
                    className="mt-1.5 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                  />
                </div>

                {error && (
                  <p className="text-sm text-red-600">{error}</p>
                )}

                <div className="flex gap-2">
                  <button
                    type="submit"
                    disabled={loading}
                    className="flex-1 rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700 disabled:opacity-50"
                  >
                    {loading ? "Updating..." : "Update"}
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      setShowStatusForm(false);
                      setStatusFormData({ newStatus: "", comment: "" });
                      setError("");
                    }}
                    className="flex-1 rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50"
                  >
                    Cancel
                  </button>
                </div>
              </form>
            </div>
          )}

          {/* Comment Form */}
          {showCommentForm && (
            <div className="rounded-lg border border-slate-200 bg-white p-4">
              <form onSubmit={handleCommentSubmit} className="space-y-3">
                <div>
                  <label className="block text-sm font-medium text-slate-700">
                    Comment<span className="text-red-500">*</span>
                  </label>
                  <textarea
                    value={commentText}
                    onChange={(e) => setCommentText(e.target.value)}
                    rows={3}
                    placeholder="Share your thoughts..."
                    className="mt-1.5 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                  />
                </div>

                {error && (
                  <p className="text-sm text-red-600">{error}</p>
                )}

                <div className="flex gap-2">
                  <button
                    type="submit"
                    disabled={loading}
                    className="flex-1 rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700 disabled:opacity-50"
                  >
                    {loading ? "Adding..." : "Add Comment"}
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      setShowCommentForm(false);
                      setCommentText("");
                      setError("");
                    }}
                    className="flex-1 rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50"
                  >
                    Cancel
                  </button>
                </div>
              </form>
            </div>
          )}
        </div>
      )}

      {/* Evaluation Timeline */}
      <div>
        <h3 className="mb-3 text-sm font-semibold text-slate-700">Activity</h3>
        <div className="space-y-3">
          {evaluations.length === 0 ? (
            <p className="text-sm text-slate-500">No evaluations yet</p>
          ) : (
            evaluations.map((evaluation) => (
              <div
                key={evaluation.id}
                className="relative rounded-lg border border-slate-200 bg-white p-4"
              >
                <div className="flex items-start gap-3">
                  <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-slate-100">
                    <span className="text-sm">
                      {evaluation.statusSnapshot ? "🔄" : "💬"}
                    </span>
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-start justify-between gap-2">
                      <p className="text-sm font-medium text-slate-900">
                        {evaluation.evaluatorName}
                      </p>
                      <p className="text-xs text-slate-500 whitespace-nowrap">
                        {formatDate(evaluation.createdAt)}
                      </p>
                    </div>
                    {evaluation.statusSnapshot && (
                      <p className="mt-1 text-xs text-slate-600">
                        Changed status to{" "}
                        <span className="font-semibold">
                          {getStatusLabel(evaluation.statusSnapshot)}
                        </span>
                      </p>
                    )}
                    <p className="mt-2 text-sm text-slate-700 whitespace-pre-wrap">
                      {evaluation.comment}
                    </p>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
