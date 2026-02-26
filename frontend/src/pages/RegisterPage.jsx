import React, { useState } from "react";
import RegisterForm from "../components/auth/RegisterForm";
import { registerUser } from "../services/authService";
import { useNavigate, Link } from "react-router-dom";
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
    <div className="min-h-screen flex flex-col lg:flex-row">
      {/* Left Panel - Branding (Hidden on Mobile) */}
      <div className="hidden lg:flex lg:w-2/5 flex-col items-center justify-center bg-gradient-to-br from-blue-950 via-slate-900 to-blue-900 px-8 py-12">
        <div className="text-center">
          <div className="mx-auto mb-8 flex h-20 w-20 items-center justify-center rounded-3xl bg-blue-600 shadow-2xl shadow-blue-500/50">
            <span className="text-4xl">💡</span>
          </div>
          <h1 className="text-4xl font-bold text-white">InnovatEPAM</h1>
          <p className="mt-3 text-xl text-blue-200">Where ideas become innovation</p>
          
          <div className="mt-12 space-y-8">
            <div className="text-left">
              <p className="flex items-start gap-3 text-slate-300">
                <span className="text-xl flex-shrink-0">✓</span>
                <span>Submit and track your innovative ideas</span>
              </p>
            </div>
            <div className="text-left">
              <p className="flex items-start gap-3 text-slate-300">
                <span className="text-xl flex-shrink-0">✓</span>
                <span>Get expert evaluation and feedback</span>
              </p>
            </div>
            <div className="text-left">
              <p className="flex items-start gap-3 text-slate-300">
                <span className="text-xl flex-shrink-0">✓</span>
                <span>Drive innovation across your organization</span>
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Right Panel - Form */}
      <div className="flex w-full lg:w-3/5 flex-col items-center justify-center bg-white px-4 py-12 sm:px-6 lg:px-12">
        <div className="w-full max-w-md">
          {/* Mobile Logo */}
          <div className="mb-8 flex lg:hidden justify-center">
            <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-blue-600 shadow-lg shadow-blue-500/30">
              <span className="text-3xl">💡</span>
            </div>
          </div>

          {/* Form Header */}
          <div className="mb-8">
            <h2 className="text-3xl font-bold text-slate-900">Create your account</h2>
            <p className="mt-2 text-slate-600">Join InnovatEPAM today</p>
          </div>

          {/* Error/Success Message */}
          {message && (
            <div className={`mb-4 rounded-lg px-4 py-3 text-sm font-medium ${
              message.toLowerCase().includes('successful')
                ? 'bg-green-50 text-green-800'
                : 'bg-red-50 text-red-800'
            }`}>
              {message}
            </div>
          )}

          {/* Register Form */}
          <RegisterForm onSubmit={handleRegister} loading={loading} />

          {/* Sign In Link */}
          <p className="mt-6 text-center text-slate-600">
            Already have an account?{" "}
            <Link
              to="/login"
              className="font-semibold text-blue-600 hover:text-blue-700 transition-colors"
            >
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
