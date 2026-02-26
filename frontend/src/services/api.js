import axios from "axios";
import { getToken } from "../utils/tokenStorage";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/v1"
});

// Request interceptor: Fetch token from localStorage for EVERY request
api.interceptors.request.use((config) => {
  // Fetch token directly from localStorage inside the interceptor to avoid stale values
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
