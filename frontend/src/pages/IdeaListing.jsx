import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useIdeas } from "../hooks/useIdeas";
import { getStatusLabel, getStatusColor, IdeaStatus } from "../utils/statusUtils";

export default function IdeaListing() {
  const navigate = useNavigate();
  const { ideas, loading, error, pagination, fetchIdeas } = useIdeas();
  const [filters, setFilters] = useState({
    status: "",
    category: "",
    page: 0,
    size: 10
  });

  useEffect(() => {
    fetchIdeas(filters);
  }, [filters]);

  const handleFilterChange = (key, value) => {
    setFilters(prev => ({ ...prev, [key]: value, page: 0 }));
  };

  const handlePageChange = (newPage) => {
    setFilters(prev => ({ ...prev, page: newPage }));
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric"
    });
  };

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-slate-900">Ideas</h1>
          <p className="mt-2 text-slate-600">
            Browse and manage innovation proposals
          </p>
        </div>
        <button
          onClick={() => navigate("/ideas/new")}
          className="flex items-center gap-2 rounded-lg bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-blue-700"
        >
          <span>➕</span>
          Submit Idea
        </button>
      </div>

      {/* Filters */}
      <div className="mb-6 flex gap-4 rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
        <div className="flex-1">
          <label className="block text-sm font-medium text-slate-700">Filter by Status</label>
          <select
            value={filters.status}
            onChange={(e) => handleFilterChange("status", e.target.value)}
            className="mt-1.5 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
          >
            <option value="">All Statuses</option>
            <option value={IdeaStatus.SUBMITTED}>Submitted</option>
            <option value={IdeaStatus.UNDER_REVIEW}>Under Review</option>
            <option value={IdeaStatus.ACCEPTED}>Accepted</option>
            <option value={IdeaStatus.REJECTED}>Rejected</option>
          </select>
        </div>
        <div className="flex-1">
          <label className="block text-sm font-medium text-slate-700">Filter by Category</label>
          <input
            type="text"
            value={filters.category}
            onChange={(e) => handleFilterChange("category", e.target.value)}
            placeholder="Enter category..."
            className="mt-1.5 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
          />
        </div>
      </div>

      {/* Error Message */}
      {error && (
        <div className="mb-6 rounded-lg border border-red-200 bg-red-50 px-4 py-3">
          <p className="text-sm text-red-800">{error}</p>
        </div>
      )}

      {/* Ideas Table */}
      {loading ? (
        <div className="flex items-center justify-center py-12">
          <div className="text-center">
            <div className="mx-auto mb-4 h-12 w-12 animate-spin rounded-full border-4 border-slate-200 border-t-blue-600"></div>
            <p className="text-sm text-slate-600">Loading ideas...</p>
          </div>
        </div>
      ) : ideas.length === 0 ? (
        <div className="rounded-xl border border-slate-200 bg-white p-12 text-center shadow-sm">
          <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-slate-100">
            <span className="text-3xl">💡</span>
          </div>
          <h3 className="text-lg font-semibold text-slate-900">No ideas found</h3>
          <p className="mt-2 text-sm text-slate-600">
            {filters.status || filters.category
              ? "Try adjusting your filters"
              : "Be the first to submit an idea"}
          </p>
        </div>
      ) : (
        <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
          <table className="w-full">
            <thead className="border-b border-slate-200 bg-slate-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-600">
                  Title
                </th>
                <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-600">
                  Category
                </th>
                <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-600">
                  Status
                </th>
                <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-600">
                  Submitter
                </th>
                <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-600">
                  Date
                </th>
                <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-600">
                  Comments
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-200">
              {ideas.map((idea) => (
                <tr
                  key={idea.id}
                  onClick={() => navigate(`/ideas/${idea.id}`)}
                  className="cursor-pointer transition-colors hover:bg-slate-50"
                >
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-2">
                      <p className="font-medium text-slate-900">{idea.title}</p>
                      {idea.hasAttachment && (
                        <span className="text-slate-400" title="Has attachment">
                          📎
                        </span>
                      )}
                    </div>
                  </td>
                  <td className="px-6 py-4 text-sm text-slate-600">
                    {idea.category}
                  </td>
                  <td className="px-6 py-4">
                    <span
                      className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ${getStatusColor(
                        idea.status
                      )}`}
                    >
                      {getStatusLabel(idea.status)}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-sm text-slate-600">
                    {idea.submitterName}
                  </td>
                  <td className="px-6 py-4 text-sm text-slate-600">
                    {formatDate(idea.createdAt)}
                  </td>
                  <td className="px-6 py-4 text-sm text-slate-600">
                    {idea.evaluationCount > 0 ? (
                      <span className="flex items-center gap-1">
                        💬 {idea.evaluationCount}
                      </span>
                    ) : (
                      <span className="text-slate-400">—</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Pagination */}
      {pagination.totalPages > 1 && (
        <div className="mt-6 flex items-center justify-between">
          <p className="text-sm text-slate-600">
            Showing {pagination.pageNumber * pagination.pageSize + 1} to{" "}
            {Math.min(
              (pagination.pageNumber + 1) * pagination.pageSize,
              pagination.totalElements
            )}{" "}
            of {pagination.totalElements} ideas
          </p>
          <div className="flex gap-2">
            <button
              onClick={() => handlePageChange(pagination.pageNumber - 1)}
              disabled={pagination.pageNumber === 0}
              className="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
            >
              Previous
            </button>
            <button
              onClick={() => handlePageChange(pagination.pageNumber + 1)}
              disabled={pagination.pageNumber >= pagination.totalPages - 1}
              className="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
            >
              Next
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
