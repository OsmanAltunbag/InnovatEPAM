import React from "react";
import useAuth from "../../hooks/useAuth";
import { useNavigate } from "react-router-dom";

export default function Layout({ children }) {
  const { user, signOut } = useAuth();
  const navigate = useNavigate();

  const handleSignOut = () => {
    signOut();
    navigate("/login");
  };

  return (
    <div className="flex min-h-screen bg-slate-50">
      {/* Sidebar */}
      <aside className="w-64 border-r border-slate-200 bg-white">
        <div className="flex h-16 items-center border-b border-slate-200 px-6">
          <h1 className="text-xl font-bold text-slate-900">InnovatEPAM</h1>
        </div>
        <nav className="p-4">
          <ul className="space-y-1">
            <li>
              <button
                onClick={() => navigate("/dashboard")}
                className="flex w-full items-center rounded-lg px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100"
              >
                <span className="mr-3">📊</span>
                Dashboard
              </button>
            </li>
            <li>
              <button
                onClick={() => navigate("/ideas")}
                className="flex w-full items-center rounded-lg px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100"
              >
                <span className="mr-3">💡</span>
                Ideas
              </button>
            </li>
            {(user?.role === "submitter" || user?.role === "admin") && (
              <li>
                <button
                  onClick={() => navigate("/ideas/new")}
                  className="flex w-full items-center rounded-lg px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100"
                >
                  <span className="mr-3">➕</span>
                  Submit Idea
                </button>
              </li>
            )}
          </ul>
        </nav>
      </aside>

      {/* Main Content */}
      <div className="flex flex-1 flex-col">
        {/* Header */}
        <header className="flex h-16 items-center justify-between border-b border-slate-200 bg-white px-6">
          <div className="flex items-center gap-4">
            <h2 className="text-lg font-semibold text-slate-900">
              Welcome back
            </h2>
          </div>
          <div className="flex items-center gap-4">
            <div className="text-right">
              <p className="text-sm font-medium text-slate-900">{user?.email}</p>
              <p className="text-xs text-slate-500 capitalize">{user?.role}</p>
            </div>
            <button
              onClick={handleSignOut}
              className="rounded-lg border border-slate-200 px-3 py-1.5 text-sm font-medium text-slate-700 hover:bg-slate-50"
            >
              Sign out
            </button>
          </div>
        </header>

        {/* Page Content */}
        <main className="flex-1 p-6">{children}</main>
      </div>
    </div>
  );
}
