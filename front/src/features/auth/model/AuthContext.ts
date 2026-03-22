import { createContext } from 'react';
import type { SessionUser } from '@/entities/auth/model';

export interface LoginPayload {
  login: string;
  password: string;
}

export interface AuthContextValue {
  user: SessionUser | null;
  isAuthenticated: boolean;
  isInitializing: boolean;
  login: (payload: LoginPayload) => Promise<void>;
  logout: () => void;
  restoreSession: () => Promise<void>;
  setUser: (user: SessionUser | null) => void;
}

export const AuthContext = createContext<AuthContextValue | null>(null);


