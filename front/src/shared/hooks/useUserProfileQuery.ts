import { useQuery } from '@tanstack/react-query';
import { usersApi } from '@/shared/api/usersApi';
import { queryKeys } from '@/shared/const/queryKeys';
import { mapUserDtoToUserProfile } from '@/entities/user/mapper';

export function useUserProfileQuery(userId: string | undefined) {
  return useQuery({
    queryKey: userId ? queryKeys.userById(userId) : ['users', 'disabled'],
    enabled: Boolean(userId),
    queryFn: async () => mapUserDtoToUserProfile(await usersApi.getUserById(userId!)),
  });
}


