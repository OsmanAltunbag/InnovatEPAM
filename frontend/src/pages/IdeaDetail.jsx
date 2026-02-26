import React, { useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useIdeas } from "../hooks/useIdeas";
import EvaluationPanel from "../components/EvaluationPanel";
import { getStatusColor, getStatusLabel, getStatusIcon } from "../utils/statusUtils";
import { formatFileSize } from "../utils/fileUploadUtils";

export default function IdeaDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { selectedIdea, fetchIdeaById, downloadAttachment, loading, error } = useIdeas({
    autoLoad: false
  });

  useEffect(() => {
    if (id) {
      fetchIdeaById(id);
    }
  }, [fetchIdeaById, id]);

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString("en-US", {
      month: "long",
      day: "numeric",
      year: "numeric"
    });
  };

  const handleDownload = async () => {
    if (selectedIdea?.attachment) {
      await downloadAttachment(
        selectedIdea.id,
        selectedIdea.attachment.id,
        selectedIdea.attachment.originalFilename
      );
    }
  };

  if (loading) {
    return (
      <div className="flex min-h-[400px] items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-slate-200 border-t-blue-600" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="rounded-lg border border-red-200 bg-red-50 p-4">
        <p className="text-sm text-red-600">{error}</p>
        <button
          onClick={() => navigate("/ideas")}
          className="mt-2 text-sm font-medium text-red-700 hover:text-red-800"
        >
          ← Back to Ideas
        </button>
      </div>
    );
  }

  if (!selectedIdea) {
    return (
      <div className="rounded-lg border border-slate-200 bg-white p-8 text-center">
        <p className="text-slate-600">Idea not found</p>
        <button
          onClick={() => navigate("/ideas")}
          className="mt-4 text-sm font-medium text-blue-600 hover:text-blue-700"
        >
          ← Back to Ideas
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Back Button */}
      <button
        onClick={() => navigate("/ideas")}
        className="flex items-center gap-2 text-sm font-medium text-slate-600 hover:text-slate-900"
      >
        <span>←</span>
        <span>Back to Ideas</span>
      </button>

      {/* Two-Column Layout */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Left Column: Idea Content (2/3 width) */}
        <div className="lg:col-span-2 space-y-6">
          {/* Idea Header Card */}
          <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
            <div className="mb-4 flex items-start justify-between gap-4">
              <h1 className="text-2xl font-bold text-slate-900">
                {selectedIdea.title}
              </h1>
              <span
                className={`inline-flex shrink-0 items-center gap-1 rounded-full px-3 py-1 text-xs font-semibold ${getStatusColor(
                  selectedIdea.status
                )}`}
              >
                <span>{getStatusIcon(selectedIdea.status)}</span>
                <span>{getStatusLabel(selectedIdea.status)}</span>
              </span>
            </div>

            {/* Metadata */}
            <div className="flex flex-wrap gap-4 border-b border-slate-200 pb-4 text-sm text-slate-600">
              <div>
                <span className="font-medium">Category:</span>{" "}
                <span className="rounded bg-slate-100 px-2 py-0.5 text-slate-700">
                  {selectedIdea.category}
                </span>
              </div>
              <div>
                <span className="font-medium">Submitted by:</span>{" "}
                {selectedIdea.submitterName}
              </div>
              <div>
                <span className="font-medium">Date:</span>{" "}
                {formatDate(selectedIdea.createdAt)}
              </div>
            </div>

            {/* Description */}
            <div className="mt-4">
              <h2 className="mb-2 text-sm font-semibold text-slate-700">
                Description
              </h2>
              <p className="whitespace-pre-wrap text-slate-700">
                {selectedIdea.description}
              </p>
            </div>
          </div>

          {/* Attachment Card */}
          {selectedIdea.attachment && (
            <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
              <h2 className="mb-3 text-sm font-semibold text-slate-700">
                Attachment
              </h2>
              <div className="flex items-center justify-between rounded-lg border border-slate-200 bg-slate-50 p-4">
                <div className="flex items-center gap-3">
                  <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-blue-100">
                    <span className="text-xl">📎</span>
                  </div>
                  <div>
                    <p className="text-sm font-medium text-slate-900">
                      {selectedIdea.attachment.originalFilename}
                    </p>
                    <p className="text-xs text-slate-500">
                      {formatFileSize(selectedIdea.attachment.fileSize)}
                    </p>
                  </div>
                </div>
                <button
                  onClick={handleDownload}
                  className="rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-700 transition-colors hover:bg-slate-50"
                >
                  Download
                </button>
              </div>
            </div>
          )}

          {/* Statistics Card */}
          <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
            <h2 className="mb-3 text-sm font-semibold text-slate-700">
              Statistics
            </h2>
            <div className="flex gap-6">
              <div>
                <p className="text-2xl font-bold text-blue-600">
                  {selectedIdea.evaluations?.length || 0}
                </p>
                <p className="text-sm text-slate-600">
                  {selectedIdea.evaluations?.length === 1
                    ? "Evaluation"
                    : "Evaluations"}
                </p>
              </div>
              <div>
                <p className="text-2xl font-bold text-slate-700">
                  {selectedIdea.attachment ? "1" : "0"}
                </p>
                <p className="text-sm text-slate-600">
                  {selectedIdea.attachment ? "Attachment" : "Attachments"}
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Right Column: Evaluation Panel (1/3 width) */}
        <div className="lg:col-span-1">
          <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm lg:sticky lg:top-6">
            <EvaluationPanel idea={selectedIdea} />
          </div>
        </div>
      </div>
    </div>
  );
}
