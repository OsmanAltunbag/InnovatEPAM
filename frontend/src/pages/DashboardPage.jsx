import React from "react";
import useAuth from "../hooks/useAuth";

export default function DashboardPage() {
  const { user } = useAuth();

  return (
    <div className="mx-auto max-w-3xl px-4 py-10">
      <h2 className="text-2xl font-semibold">Dashboard</h2>
      <p className="mt-2 text-slate-600">You are signed in.</p>
      {user ? (
        <div className="mt-6 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <p className="text-sm text-slate-500">Email</p>
          <p className="text-lg font-medium">{user.email}</p>
          <p className="mt-4 text-sm text-slate-500">Role</p>
          <p className="text-lg font-medium">{user.role}</p>
        </div>
      ) : null}
    </div>
  );
}
