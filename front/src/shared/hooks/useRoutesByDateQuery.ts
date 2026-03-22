import { useQuery } from '@tanstack/react-query';
import { routesApi } from '@/shared/api/routesApi';
import { queryKeys } from '@/shared/const/queryKeys';
import { mapRouteDtoToAppRoute } from '@/entities/route/mapper';

export function useRoutesByDateQuery(userId: string | undefined, date: string) {
  return useQuery({
    queryKey: userId ? queryKeys.routesByUserDate(userId, date) : ['routes', 'disabled'],
    enabled: Boolean(userId),
    queryFn: async () => {
      const data = await routesApi.getRoutesForUserAndDate(userId!, date);
      return data.map(mapRouteDtoToAppRoute);
    },
  });
}


