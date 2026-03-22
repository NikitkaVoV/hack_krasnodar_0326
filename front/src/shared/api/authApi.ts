import { http } from '@/shared/api/http';
import type {
  AuthResponseDto,
  LoginRequestDto,
  UserResponseDto,
  RefreshTokenRequestDto,
} from '@/entities/auth/dto';

export const authApi = {
  async login(payload: LoginRequestDto): Promise<AuthResponseDto> {
    const { data } = await http.post<AuthResponseDto>('/api/auth/login', payload);
    return data;
  },
  async refresh(payload: RefreshTokenRequestDto): Promise<AuthResponseDto> {
    const { data } = await http.post<AuthResponseDto>('/api/auth/refresh', payload);
    return data;
  },
  async me(): Promise<UserResponseDto> {
    const { data } = await http.get<UserResponseDto>('/api/auth/me');
    return data;
  },
};


