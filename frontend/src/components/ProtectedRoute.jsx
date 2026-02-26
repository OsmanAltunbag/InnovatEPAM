import React from 'react';
import useAuth from '../hooks/useAuth';

export default function ProtectedRoute({ children }) {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return (
      <div className="mx-auto max-w-2xl px-4 py-10">
        <h2 className="text-2xl font-semibold">Access denied</h2>
        <p className="mt-2 text-slate-600">Please log in to continue.</p>
      </div>
    );
  }

  return children;
}
