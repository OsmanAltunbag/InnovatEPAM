import React, { createContext, useCallback, useMemo, useState } from "react";
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
  const [token, setTokenState] = useState(getToken());

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
      isAuthenticated: Boolean(token)
    }),
    [token, user, signIn, signOut]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export default AuthContext;
