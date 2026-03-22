export interface LoginRequestDto {
  login: string;
  password: string;
}

export interface RefreshTokenRequestDto {
  refreshToken: string;
}

export interface UserResponseDto {
  id: string;
  login: string;
  name: string;
  userType: string;
}

export interface AuthResponseDto {
  accessToken: string;
  refreshToken: string;
  user: UserResponseDto;
}


