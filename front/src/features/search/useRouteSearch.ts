import { useMemo } from 'react';
import type { AppRoute } from '@/entities/route/model';

export function useRouteSearch(routes: AppRoute[], search: string) {
  return useMemo(() => {
    const normalized = search.trim().toLowerCase();
    if (!normalized) {
      return routes;
    }
    return routes.filter((route) => route.summary.toLowerCase().includes(normalized));
  }, [routes, search]);
}


