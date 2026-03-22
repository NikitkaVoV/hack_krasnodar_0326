import { useMemo } from 'react';
import type { AppRoute } from '@/entities/route/model';

export type RouteSortType = 'date' | 'alphabet' | 'duration';

export function useSortedRoutes(routes: AppRoute[], sortType: RouteSortType) {
  return useMemo(() => {
    const copy = [...routes];
    if (sortType === 'alphabet') {
      return copy.sort((a, b) => a.summary.localeCompare(b.summary, 'ru'));
    }
    if (sortType === 'duration') {
      return copy.sort((a, b) => b.totalDurationMinutes - a.totalDurationMinutes);
    }
    return copy.sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime());
  }, [routes, sortType]);
}


