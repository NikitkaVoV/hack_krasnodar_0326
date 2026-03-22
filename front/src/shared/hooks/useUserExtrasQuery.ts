import { useQuery } from '@tanstack/react-query';
import { usersApi } from '@/shared/api/usersApi';

export function useUserExtrasQuery(userId: string | undefined) {
  return useQuery({
    queryKey: userId ? ['users', 'extras', userId] : ['users', 'extras', 'disabled'],
    enabled: Boolean(userId),
    queryFn: async () => {
      const safeFetch = async <T>(fn: () => Promise<T>) => {
        try {
          return await fn();
        } catch {
          return null;
        }
      };

      const [preferences, tags, constraints] = await Promise.all([
        safeFetch(() => usersApi.getUserPreferences(userId!)),
        safeFetch(() => usersApi.getUserTags(userId!)),
        safeFetch(() => usersApi.getUserConstraints(userId!)),
      ]);

      return { preferences, tags, constraints };
    },
  });
}

