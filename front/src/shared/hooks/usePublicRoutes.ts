import { useQuery } from '@tanstack/react-query';
import { publicRoutesApi } from '@/shared/api/publicRoutesApi';
import type { PublicRouteCard, PublicRouteDetails } from '@/entities/publicRoute/model';

export function usePublicRoutesCatalogQuery(limit = 24, date?: string) {
  return useQuery<PublicRouteCard[]>({
    queryKey: ['public-routes', 'catalog', limit, date ?? ''],
    queryFn: () => publicRoutesApi.getPublicRoutesCatalog(limit, date),
  });
}

export function usePublicRouteDetailsQuery(routeId: string | null) {
  return useQuery<PublicRouteDetails>({
    queryKey: ['public-routes', 'details', routeId],
    enabled: Boolean(routeId),
    queryFn: () => publicRoutesApi.getPublicRouteDetails(routeId!),
    retry: 0,
    refetchOnWindowFocus: false,
  });
}
