import { useMutation } from '@tanstack/react-query';
import { authApi } from '@/shared/api/authApi';
import type { LoginRequestDto } from '@/entities/auth/dto';

export function useLoginMutation() {
  return useMutation({
    mutationFn: (payload: LoginRequestDto) => authApi.login(payload),
  });
}


