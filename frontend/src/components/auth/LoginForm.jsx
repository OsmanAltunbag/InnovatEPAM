import React, { useState } from "react";
import { isValidEmail } from "../../utils/validators";

export default function LoginForm({ onSubmit, loading }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [errors, setErrors] = useState({});

  const validate = () => {
    const nextErrors = {};
    if (!isValidEmail(email)) {
      nextErrors.email = "Enter a valid email address.";
    }
    if (!password) {
      nextErrors.password = "Enter your password.";
    }
    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    if (!validate()) {
      return;
    }
    onSubmit({ email, password });
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label htmlFor="email" className="block text-sm font-medium text-slate-700">Email</label>
        <input
          id="email"
          className="mt-1.5 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
          type="email"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
        />
        {errors.email ? <p className="mt-1 text-sm text-red-600">{errors.email}</p> : null}
      </div>

      <div>
        <label htmlFor="password" className="block text-sm font-medium text-slate-700">Password</label>
        <input
          id="password"
          className="mt-1.5 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
          type="password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
        />
        {errors.password ? <p className="mt-1 text-sm text-red-600">{errors.password}</p> : null}
      </div>

      <button
        className="w-full rounded-lg bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-blue-700 disabled:opacity-50"
        type="submit"
        disabled={loading}
      >
        {loading ? "Signing in..." : "Sign in"}
      </button>
    </form>
  );
}

