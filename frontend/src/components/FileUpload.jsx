import React from "react";
import { formatFileSize, ALLOWED_EXTENSIONS } from "../utils/fileUploadUtils";

export default function FileUpload({ file, onFileSelect, onFileClear, error }) {
  const [isDragging, setIsDragging] = React.useState(false);

  const handleDragOver = (e) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = (e) => {
    e.preventDefault();
    setIsDragging(false);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    setIsDragging(false);
    const droppedFile = e.dataTransfer.files?.[0];
    if (droppedFile) {
      onFileSelect(droppedFile);
    }
  };

  const handleFileInputChange = (e) => {
    const selectedFile = e.target.files?.[0];
    if (selectedFile) {
      onFileSelect(selectedFile);
    }
  };

  return (
    <div className="space-y-2">
      <label className="block text-sm font-medium text-slate-700">
        Attachment <span className="text-slate-500">(optional)</span>
      </label>
      
      {!file ? (
        <div
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onDrop={handleDrop}
          className={`relative flex flex-col items-center justify-center rounded-lg border-2 border-dashed px-6 py-8 transition-colors ${
            isDragging
              ? "border-blue-400 bg-blue-50"
              : "border-slate-300 bg-slate-50"
          }`}
        >
          <div className="text-center">
            <div className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-full bg-slate-200">
              <span className="text-2xl">📎</span>
            </div>
            <p className="text-sm font-medium text-slate-900">
              Drop your file here, or{" "}
              <label className="cursor-pointer text-blue-600 hover:text-blue-700">
                browse
                <input
                  type="file"
                  className="hidden"
                  accept={ALLOWED_EXTENSIONS.join(",")}
                  onChange={handleFileInputChange}
                />
              </label>
            </p>
            <p className="mt-1 text-xs text-slate-500">
              PDF or PNG, max 50MB
            </p>
          </div>
        </div>
      ) : (
        <div className="flex items-center justify-between rounded-lg border border-slate-200 bg-white px-4 py-3">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-blue-100">
              <span className="text-lg">📄</span>
            </div>
            <div>
              <p className="text-sm font-medium text-slate-900">{file.name}</p>
              <p className="text-xs text-slate-500">{formatFileSize(file.size)}</p>
            </div>
          </div>
          <button
            type="button"
            onClick={onFileClear}
            className="rounded-lg p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
          >
            <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      )}
      
      {error && <p className="text-sm text-red-600">{error}</p>}
    </div>
  );
}
