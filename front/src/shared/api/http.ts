import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { env } from '@/shared/config/env';
import {
  clearTokens,
  getAccessToken,
  getRefreshToken,
  setTokens,
} from '@/shared/api/tokenStorage';
import type { AuthResponseDto } from '@/entities/auth/dto';
import { emitAuthLogout } from '@/shared/lib/authEvents';

interface RetryableRequestConfig extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

const AUTH_LOGIN_URL = '/api/auth/login';
const AUTH_REFRESH_URL = '/api/auth/refresh';

export const http = axios.create({
  baseURL: env.apiBaseUrl,
  headers: {
    'Content-Type': 'application/json',
  },
});

const refreshClient = axios.create({
  baseURL: env.apiBaseUrl,
  headers: {
    'Content-Type': 'application/json',
  },
});

let refreshPromise: Promise<string | null> | null = null;

function isAuthEndpoint(url?: string): boolean {
  return Boolean(url?.includes(AUTH_LOGIN_URL) || url?.includes(AUTH_REFRESH_URL));
}

async function refreshAccessToken(): Promise<string | null> {
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    return null;
  }

  if (!refreshPromise) {
    refreshPromise = refreshClient
      .post<AuthResponseDto>(AUTH_REFRESH_URL, { refreshToken })
      .then((response) => {
        setTokens(response.data.accessToken, response.data.refreshToken);
        return response.data.accessToken;
      })
      .catch(() => {
        clearTokens();
        return null;
      })
      .finally(() => {
        refreshPromise = null;
      });
  }

  return refreshPromise;
}

http.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

http.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as RetryableRequestConfig | undefined;
    const status = error.response?.status;
    const requestUrl = originalRequest?.url;

    if (
      !originalRequest ||
      status !== 401 ||
      originalRequest._retry ||
      isAuthEndpoint(requestUrl)
    ) {
      return Promise.reject(error);
    }

    const hadSession = Boolean(getAccessToken() || getRefreshToken());
    originalRequest._retry = true;
    const nextToken = await refreshAccessToken();

    if (!nextToken) {
      if (hadSession) {
        clearTokens();
        emitAuthLogout();
      }
      return Promise.reject(error);
    }

    originalRequest.headers.Authorization = `Bearer ${nextToken}`;
    return http.request(originalRequest);
  },
);


