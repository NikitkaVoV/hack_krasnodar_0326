import { useCallback, useEffect, useMemo, useState, type PropsWithChildren } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import axios from 'axios';
import { AuthContext, type LoginPayload } from './AuthContext';
import type { SessionUser } from '@/entities/auth/model';
import { authApi } from '@/shared/api/authApi';
import {
  clearTokens,
  getRefreshToken,
  setTokens,
  getAccessToken,
} from '@/shared/api/tokenStorage';
import { appRoutes } from '@/shared/const/routes';
import { mapUserResponseDtoToSessionUser } from '@/entities/auth/mapper';
import { onAuthLogout } from '@/shared/lib/authEvents';

export function AuthProvider({ children }: PropsWithChildren) {
  const [user, setUser] = useState<SessionUser | null>(null);
  const [isInitializing, setIsInitializing] = useState(true);
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const logout = useCallback(() => {
    clearTokens();
    setUser(null);
    queryClient.clear();
    navigate(appRoutes.login, { replace: true });
  }, [navigate, queryClient]);

  const login = useCallback(async (payload: LoginPayload) => {
    const response = await authApi.login(payload);
    setTokens(response.accessToken, response.refreshToken);
    setUser(mapUserResponseDtoToSessionUser(response.user));
  }, []);

  const restoreSession = useCallback(async () => {
    const accessToken = getAccessToken();
    const refreshToken = getRefreshToken();

    if (!accessToken && !refreshToken) {
      setUser(null);
      return;
    }

    try {
      const me = await authApi.me();
      setUser(mapUserResponseDtoToSessionUser(me));
      return;
    } catch (error) {
      if (!axios.isAxiosError(error) || error.response?.status !== 401 || !refreshToken) {
        clearTokens();
        setUser(null);
        return;
      }
    }

    try {
      const refreshed = await authApi.refresh({ refreshToken });
      setTokens(refreshed.accessToken, refreshed.refreshToken);
      const me = await authApi.me();
      setUser(mapUserResponseDtoToSessionUser(me));
    } catch {
      clearTokens();
      setUser(null);
    }
  }, []);

  useEffect(() => {
    let mounted = true;

    void (async () => {
      await restoreSession();
      if (mounted) {
        setIsInitializing(false);
      }
    })();

    return () => {
      mounted = false;
    };
  }, [restoreSession]);

  useEffect(() => onAuthLogout(logout), [logout]);

  const value = useMemo(
    () => ({
      user,
      isAuthenticated: Boolean(user),
      isInitializing,
      login,
      logout,
      restoreSession,
      setUser,
    }),
    [isInitializing, login, logout, restoreSession, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}


