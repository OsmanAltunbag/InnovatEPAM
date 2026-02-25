import React from "react";
import { BrowserRouter, Link, Route, Routes } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import DashboardPage from "./pages/DashboardPage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import ProtectedRoute from "./components/ProtectedRoute";

const Home = () => (
  <div className="mx-auto max-w-2xl px-4 py-10">
    <h1 className="text-3xl font-semibold">InnovatEPAM Portal</h1>
    <p className="mt-2 text-slate-600">
      Build your account to access submissions and evaluations.
    </p>
    <div className="mt-6 flex gap-3">
      <Link
        className="rounded-md bg-brand-600 px-4 py-2 text-white hover:bg-brand-700"
        to="/register"
      >
        Register
      </Link>
      <Link
        className="rounded-md border border-slate-200 px-4 py-2 text-slate-700 hover:border-slate-300"
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
                  <DashboardPage />
                </ProtectedRoute>
              }
            />
          </Routes>
        </div>
      </BrowserRouter>
    </AuthProvider>
  );
}
