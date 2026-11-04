import { createContext, useContext, useMemo, useState } from 'react';
import { api, clearToken, getToken, parseToken, setToken } from '../api/client.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const token = getToken();
    return token ? parseToken(token) : null;
  });

  const value = useMemo(
    () => ({
      user,
      isAuthenticated: Boolean(user),
      async login(email, password) {
        const { token } = await api.login(email, password);
        setToken(token);
        setUser(parseToken(token));
      },
      logout() {
        clearToken();
        setUser(null);
      },
    }),
    [user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
