import React, { createContext, useCallback, useEffect, useMemo, useState } from "react";
import { clearToken, getToken, setToken } from "../utils/tokenStorage";

const AuthContext = createContext(null);

const decodeJwt = (token) => {
  try {
    const payload = token.split(".")[1];
    if (!payload) {
      return null;
    }
    const decoded = atob(payload.replace(/-/g, "+").replace(/_/g, "/"));
    return JSON.parse(decoded);
  } catch (error) {
    return null;
  }
};

export const AuthProvider = ({ children }) => {
  // Initialize token from localStorage synchronously
  const [token, setTokenState] = useState(getToken());
  // Start as false - will be set to true in useEffect after initialization
  const [isSessionReady, setIsSessionReady] = useState(false);

  // Ensure token and isSessionReady states are fully initialized from localStorage
  // before the rest of the app tries to make authorized requests
  useEffect(() => {
    // Synchronously load token from localStorage if not already loaded
    const storedToken = getToken();
    if (storedToken !== token) {
      setTokenState(storedToken);
    }
    // Mark session as ready after localStorage initialization is complete
    setIsSessionReady(true);
  }, []);

  const user = useMemo(() => {
    if (!token) {
      return null;
    }
    const payload = decodeJwt(token);
    if (!payload) {
      return null;
    }
    return {
      email: payload.sub,
      role: payload.role,
      userId: payload.userId
    };
  }, [token]);

  const signIn = useCallback((newToken) => {
    setToken(newToken);
    setTokenState(newToken);
  }, []);

  const signOut = useCallback(() => {
    clearToken();
    setTokenState(null);
  }, []);

  const value = useMemo(
    () => ({
      token,
      user,
      signIn,
      signOut,
      isAuthenticated: Boolean(token),
      isSessionReady
    }),
    [token, user, signIn, signOut, isSessionReady]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export default AuthContext;
