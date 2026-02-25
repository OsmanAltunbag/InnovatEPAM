import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import LoginForm from "../components/auth/LoginForm";
import { loginUser } from "../services/authService";
import useAuth from "../hooks/useAuth";

export default function LoginPage() {
  const { signIn } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");

  const handleLogin = async (payload) => {
    setLoading(true);
    setMessage("");
    try {
      const response = await loginUser(payload);
      signIn(response.token);
      setMessage("Login successful.");
      // Redirect to dashboard after successful login
      setTimeout(() => {
        navigate("/dashboard");
      }, 500);
    } catch (error) {
      const apiMessage = error?.response?.data?.message;
      setMessage(apiMessage || "Login failed. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mx-auto max-w-lg py-12">
      <h2 className="text-2xl font-semibold">Welcome back</h2>
      <p className="mt-2 text-slate-600">Sign in to continue to the portal.</p>
      <div className="mt-6 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
        <LoginForm onSubmit={handleLogin} loading={loading} />
        {message ? <p className="mt-4 text-sm text-slate-700">{message}</p> : null}
      </div>
    </div>
  );
}
