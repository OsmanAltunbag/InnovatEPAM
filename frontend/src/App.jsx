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
  <div className="mx-auto max-w-2xl px-4 py-10">
    <h1 className="text-3xl font-bold text-slate-900">InnovatEPAM Portal</h1>
    <p className="mt-2 text-slate-600">
      Build your account to access submissions and evaluations.
    </p>
    <div className="mt-6 flex gap-3">
      <Link
        className="rounded-lg bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-blue-700"
        to="/register"
      >
        Register
      </Link>
      <Link
        className="rounded-lg border border-slate-300 bg-white px-4 py-2.5 text-sm font-semibold text-slate-700 transition-colors hover:bg-slate-50"
        to="/login"
      >
        Login
      </Link>
    </div>
  </div>
);

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <div className="min-h-screen bg-slate-50 text-slate-900">
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
