import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import type { PublicRouteCard } from '@/entities/publicRoute/model';
import { meRoutesApi } from '@/shared/api/meRoutesApi';

const MY_ROUTES_QUERY_KEY = ['me', 'routes'] as const;

export function useMyRoutesHistoryQuery() {
  return useQuery<PublicRouteCard[]>({
    queryKey: MY_ROUTES_QUERY_KEY,
    queryFn: () => meRoutesApi.getMyRoutes(),
    retry: 0,
    refetchOnWindowFocus: false,
  });
}

export function useDeleteMyRouteMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (routeId: string) => meRoutesApi.deleteMyRoute(routeId),
    onSuccess: (_, routeId) => {
      queryClient.setQueryData<PublicRouteCard[] | undefined>(MY_ROUTES_QUERY_KEY, (prev) =>
        (prev ?? []).filter((route) => route.id !== routeId),
      );
    },
  });
}
