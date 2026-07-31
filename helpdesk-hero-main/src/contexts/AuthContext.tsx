// ============================================================
// AuthContext — manages current user session (mock, no backend)
// ============================================================
import React, { createContext, useContext, useState, useEffect } from "react";
import type { User, Role, AuthContextValue } from "@/types";
import { apiFetch } from "@/lib/api";
import { getCurrentUser } from "@/lib/api";

const AuthContext = createContext<AuthContextValue | null>(null);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  /** Login with email + password locally to backend */
  const login = async (email: string, password: string) => {
    try {
      const response = await apiFetch("/api/auth/login", {
        method: "POST",
        body: JSON.stringify({ email, password }),
      });
      const { user, token } = response;
      localStorage.setItem("token", token);
      
      const mappedUser: User = { ...user, role: user.role.toLowerCase() };
      setCurrentUser(mappedUser);
      sessionStorage.setItem("hd_user", JSON.stringify(mappedUser));
      return { success: true, message: "Welcome back!", user: mappedUser };
    } catch (error: any) {
      return { success: false, message: error.message || "Invalid email or password." };
    }
  };

  /** Register a new account against backend */
  const register = async (name: string, email: string, password: string, role: Role = "user") => {
    try {
      const response = await apiFetch("/api/auth/register", {
        method: "POST",
        body: JSON.stringify({ name, email, password, role: role.toUpperCase() }),
      });
      const { user, token } = response;
      localStorage.setItem("token", token);
      
      const mappedUser: User = { ...user, role: user.role.toLowerCase() };
      setCurrentUser(mappedUser);
      sessionStorage.setItem("hd_user", JSON.stringify(mappedUser));
      return { success: true, message: "Account created successfully!" };
    } catch (error: any) {
      return { success: false, message: error.message || "Registration failed." };
    }
  };

  useEffect(() => {
    const initAuth = async () => {
      const token = localStorage.getItem("token");
      if (token && !currentUser) {
        try {
          const user = await getCurrentUser();
          const mappedUser: User = { ...user, role: user.role.toLowerCase() as Role };
          setCurrentUser(mappedUser);
          sessionStorage.setItem("hd_user", JSON.stringify(mappedUser));
        } catch (err) {
          console.error('Auth refresh failed', err);
          localStorage.removeItem("token");
        }
      }
      setLoading(false);
    };
    initAuth();
  }, []);

  const logout = () => {
    setCurrentUser(null);
    sessionStorage.removeItem("hd_user");
    localStorage.removeItem("token");
  };

  if (loading) {
    return <div>Loading...</div>;
  }

  return (
    <AuthContext.Provider value={{ currentUser, login, register, logout, isAuthenticated: !!currentUser }}>
      {children}
    </AuthContext.Provider>
  );
};

/** Hook to access auth context */
export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside <AuthProvider>");
  return ctx;
};
