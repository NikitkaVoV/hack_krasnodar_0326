import { useQuery } from '@tanstack/react-query';
import { authApi } from '@/shared/api/authApi';
import { queryKeys } from '@/shared/const/queryKeys';
import { mapUserResponseDtoToSessionUser } from '@/entities/auth/mapper';

export function useCurrentUserQuery(enabled = true) {
  return useQuery({
    queryKey: queryKeys.authMe,
    queryFn: async () => mapUserResponseDtoToSessionUser(await authApi.me()),
    enabled,
  });
}


