import { createContext, useContext, useMemo, useState } from 'react';
import axiosClient from '../api/axiosClient';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [role, setRole] = useState(localStorage.getItem('role'));

  const normalizeEmail = (email) => (email || '').trim().toLowerCase();

  const login = async (email, password) => {
    const response = await axiosClient.post('/auth/login', { email: normalizeEmail(email), password });
    const { token: jwtToken, role: userRole } = response.data;
    localStorage.setItem('token', jwtToken);
    localStorage.setItem('role', userRole);
    setToken(jwtToken);
    setRole(userRole);
    return userRole;
  };

  const register = async (payload) => {
    await axiosClient.post('/auth/register', {
      ...payload,
      email: normalizeEmail(payload?.email),
    });
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    setToken(null);
    setRole(null);
  };

  const value = useMemo(
    () => ({ token, role, isAuthenticated: !!token, login, register, logout }),
    [token, role]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
