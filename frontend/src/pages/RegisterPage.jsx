import React, { useState } from "react";
import RegisterForm from "../components/auth/RegisterForm";
import { registerUser } from "../services/authService";
import { useNavigate } from "react-router-dom";
import useAuth from "../hooks/useAuth";

export default function RegisterPage() {
  const { signIn } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");

  const handleRegister = async (payload) => {
    setLoading(true);
    setMessage("");
    try {
      const response = await registerUser(payload);
      signIn(response.token);
      setMessage("Registration successful. Redirecting to dashboard...");
      navigate("/dashboard");
    } catch (error) {
      const apiMessage = error?.response?.data?.message;
      setMessage(apiMessage || "Registration failed. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mx-auto max-w-lg py-12">
      <h2 className="text-2xl font-semibold">Create your account</h2>
      <p className="mt-2 text-slate-600">
        Register with your email, choose a role, and get started.
      </p>
      <div className="mt-6 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
        <RegisterForm onSubmit={handleRegister} loading={loading} />
        {message ? <p className="mt-4 text-sm text-slate-700">{message}</p> : null}
      </div>
    </div>
  );
}
