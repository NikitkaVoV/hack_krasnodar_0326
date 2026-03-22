export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
}

export interface SessionUser {
  id: string;
  login: string;
  name: string;
  userType: string;
}


