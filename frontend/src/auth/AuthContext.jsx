import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { getMyInfo, login as loginRequest, logout as logoutRequest, signup as signupRequest } from '../api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [isAuthLoading, setIsAuthLoading] = useState(true);

  const restoreAuth = useCallback(async () => {
    try {
      const me = await getMyInfo();
      setUser(me);
      return me;
    } catch (error) {
      setUser(null);
      return null;
    } finally {
      setIsAuthLoading(false);
    }
  }, []);

  useEffect(() => {
    restoreAuth();
  }, [restoreAuth]);

  const loginWithEmail = useCallback(async (credentials) => {
    await loginRequest(credentials);
    const me = await getMyInfo();
    setUser(me);
    return me;
  }, []);

  const signupWithEmail = useCallback(async (payload) => {
    const response = await signupRequest(payload);
    return response;
  }, []);

  const logout = useCallback(async () => {
    try {
      await logoutRequest();
    } finally {
      setUser(null);
    }
  }, []);

  const value = useMemo(() => {
    return {
      user,
      isAuthenticated: Boolean(user),
      isAuthLoading,
      restoreAuth,
      loginWithEmail,
      signupWithEmail,
      logout,
    };
  }, [isAuthLoading, loginWithEmail, logout, restoreAuth, signupWithEmail, user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }

  return context;
}
