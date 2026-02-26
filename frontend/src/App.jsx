import React from "react";
import { BrowserRouter, Link, Route, Routes } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import DashboardPage from "./pages/DashboardPage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import ProtectedRoute from "./components/ProtectedRoute";
import Layout from "./components/Layout";
import IdeaListing from "./pages/IdeaListing";
import IdeaForm from "./pages/IdeaForm";
import IdeaDetail from "./pages/IdeaDetail";

const Home = () => (
  <div className="min-h-screen bg-gradient-to-br from-blue-950 via-slate-900 to-blue-900">
    {/* Main Hero Section */}
    <div className="flex min-h-screen flex-col items-center justify-center px-4 py-12">
      <div className="text-center">
        {/* Logo Circle */}
        <div className="mx-auto mb-8 flex h-20 w-20 items-center justify-center rounded-3xl bg-blue-600 shadow-2xl shadow-blue-500/50">
          <span className="text-4xl">💡</span>
        </div>
        
        {/* Title */}
        <h1 className="text-5xl font-bold text-white md:text-6xl">
          InnovatEPAM
        </h1>
        
        {/* Tagline */}
        <p className="mt-4 text-xl text-blue-200">
          Where ideas become innovation
        </p>
        
        {/* Description */}
        <p className="mx-auto mt-6 max-w-xl text-slate-300">
          Submit, evaluate, and track innovative ideas across your organization. Empower your team to drive meaningful change.
        </p>
        
        {/* CTA Buttons */}
        <div className="mt-10 flex flex-col gap-4 sm:flex-row sm:justify-center">
          <Link
            to="/register"
            className="rounded-xl bg-blue-600 px-8 py-4 text-lg font-semibold text-white shadow-lg shadow-blue-500/40 transition-all hover:bg-blue-500 hover:shadow-blue-500/60 hover:scale-105 active:scale-95"
          >
            Get Started
          </Link>
          <Link
            to="/login"
            className="rounded-xl border-2 border-white/30 bg-white/10 px-8 py-4 text-lg font-semibold text-white backdrop-blur-md transition-all hover:border-white/50 hover:bg-white/20"
          >
            Sign In
          </Link>
        </div>
      </div>
    </div>

    {/* Feature Highlights */}
    <div className="border-t border-white/10 bg-gradient-to-b from-transparent to-black/20 px-4 py-20">
      <div className="mx-auto max-w-5xl">
        <div className="grid grid-cols-1 gap-8 md:grid-cols-3">
          {/* Feature 1 */}
          <div className="flex flex-col items-center text-center">
            <div className="text-5xl mb-4">💡</div>
            <h3 className="text-lg font-semibold text-white">Submit Ideas</h3>
            <p className="mt-2 text-slate-300">
              Share your innovative thoughts and visions with the organization.
            </p>
          </div>
          
          {/* Feature 2 */}
          <div className="flex flex-col items-center text-center">
            <div className="text-5xl mb-4">🔍</div>
            <h3 className="text-lg font-semibold text-white">Expert Review</h3>
            <p className="mt-2 text-slate-300">
              Get valuable feedback and evaluation from specialists.
            </p>
          </div>
          
          {/* Feature 3 */}
          <div className="flex flex-col items-center text-center">
            <div className="text-5xl mb-4">✨</div>
            <h3 className="text-lg font-semibold text-white">Track Progress</h3>
            <p className="mt-2 text-slate-300">
              Follow your idea's journey from submission to implementation.
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
);

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <div className="min-h-screen text-slate-900">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route
              path="/dashboard"
              element={
                <ProtectedRoute>
                  <Layout>
                    <DashboardPage />
                  </Layout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/ideas"
              element={
                <ProtectedRoute>
                  <Layout>
                    <IdeaListing />
                  </Layout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/ideas/new"
              element={
                <ProtectedRoute>
                  <Layout>
                    <IdeaForm />
                  </Layout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/ideas/:id"
              element={
                <ProtectedRoute>
                  <Layout>
                    <IdeaDetail />
                  </Layout>
                </ProtectedRoute>
              }
            />
          </Routes>
        </div>
      </BrowserRouter>
    </AuthProvider>
  );
}
