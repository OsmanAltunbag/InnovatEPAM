import React from "react";
import { useNavigate } from "react-router-dom";
import useAuth from "../hooks/useAuth";
import { canSubmit, canEvaluate } from "../utils/roleUtils";

export default function DashboardPage() {
  const { user } = useAuth();
  const navigate = useNavigate();

  return (
    <div className="mx-auto max-w-5xl space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-slate-900">Dashboard</h2>
        <p className="mt-1 text-slate-600">Welcome to InnovatEPAM Portal</p>
      </div>

      {user && (
        <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-slate-900">Your Profile</h3>
          <div className="mt-4 grid grid-cols-2 gap-4">
            <div>
              <p className="text-sm font-medium text-slate-500">Email</p>
              <p className="mt-1 text-slate-900">{user.email}</p>
            </div>
            <div>
              <p className="text-sm font-medium text-slate-500">Role</p>
              <p className="mt-1 capitalize text-slate-900">{user.role}</p>
            </div>
          </div>
        </div>
      )}

      <div>
        <h3 className="text-lg font-semibold text-slate-900">Quick Actions</h3>
        <div className="mt-4 grid grid-cols-1 gap-4 md:grid-cols-3">
          <button
            onClick={() => navigate("/ideas")}
            className="rounded-xl border border-slate-200 bg-white p-6 text-left shadow-sm transition-all hover:border-blue-300 hover:shadow-md"
          >
            <div className="text-3xl">💡</div>
            <h4 className="mt-3 font-semibold text-slate-900">Browse Ideas</h4>
            <p className="mt-1 text-sm text-slate-600">
              View all submitted ideas and track their status
            </p>
          </button>

          {canSubmit(user?.role) && (
            <button
              onClick={() => navigate("/ideas/new")}
              className="rounded-xl border border-slate-200 bg-white p-6 text-left shadow-sm transition-all hover:border-blue-300 hover:shadow-md"
            >
              <div className="text-3xl">➕</div>
              <h4 className="mt-3 font-semibold text-slate-900">Submit Idea</h4>
              <p className="mt-1 text-sm text-slate-600">
                Share your innovative ideas with the team
              </p>
            </button>
          )}

          {canEvaluate(user?.role) && (
            <button
              onClick={() => navigate("/ideas")}
              className="rounded-xl border border-slate-200 bg-white p-6 text-left shadow-sm transition-all hover:border-blue-300 hover:shadow-md"
            >
              <div className="text-3xl">✅</div>
              <h4 className="mt-3 font-semibold text-slate-900">Evaluate Ideas</h4>
              <p className="mt-1 text-sm text-slate-600">
                Review and provide feedback on submitted ideas
              </p>
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
