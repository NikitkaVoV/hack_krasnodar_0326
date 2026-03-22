import { useQuery } from '@tanstack/react-query';
import { queryKeys } from '@/shared/const/queryKeys';
import { homeApi } from '@/shared/api/homeApi';
import {
  mapEventDtoToEventCard,
  mapRouteCollectionDtoToModel,
  mapRouteDtoToHomeRouteCard,
} from '@/entities/home/mapper';
import type { EventCard, HomeRouteCard, RouteCollection } from '@/entities/home/model';

interface HomePageData {
  publicRoutes: HomeRouteCard[];
  popularRoutes: HomeRouteCard[];
  recommendedRoutes: HomeRouteCard[];
  upcomingEvents: EventCard[];
  collections: RouteCollection[];
  recommendationError: boolean;
}

async function withStepsCount(routes: HomeRouteCard[]): Promise<HomeRouteCard[]> {
  if (!routes.length) {
    return routes;
  }

  return Promise.all(
    routes.map(async (route) => ({
      ...route,
      stepsCount: await homeApi.getRouteStepsCount(route.id),
    })),
  );
}

function emptyHomePageData(): HomePageData {
  return {
    publicRoutes: [],
    popularRoutes: [],
    recommendedRoutes: [],
    upcomingEvents: [],
    collections: [],
    recommendationError: false,
  };
}

export function useHomePageData(params: { date: string; limit?: number; isAuthenticated: boolean }) {
  const { date, isAuthenticated, limit = 6 } = params;

  return useQuery<HomePageData>({
    queryKey: queryKeys.home(date, limit, isAuthenticated),
    staleTime: 0,
    refetchOnMount: 'always',
    refetchOnWindowFocus: true,
    queryFn: async () => {
      const dateTo = new Date(Date.now() + 1000 * 60 * 60 * 24 * 14).toISOString().slice(0, 10);

      const aggregateResult = await homeApi
        .getHomeAggregate(date, limit)
        .then((data) => ({ ok: true as const, data }))
        .catch(() => ({ ok: false as const, data: null }));

      if (aggregateResult.ok && aggregateResult.data) {
        return {
          publicRoutes: await withStepsCount(
            (aggregateResult.data.publicRoutes ?? []).map(mapRouteDtoToHomeRouteCard),
          ),
          popularRoutes: await withStepsCount(
            (aggregateResult.data.popularRoutes ?? []).map(mapRouteDtoToHomeRouteCard),
          ),
          recommendedRoutes: await withStepsCount(
            (aggregateResult.data.recommendedRoutes ?? []).map(mapRouteDtoToHomeRouteCard),
          ),
          upcomingEvents: (aggregateResult.data.upcomingEvents ?? []).map(mapEventDtoToEventCard),
          collections: (aggregateResult.data.collections ?? []).map(mapRouteCollectionDtoToModel),
          recommendationError: false,
        };
      }

      const [publicRes, popularRes, collectionsRes, eventsRes, recommendedRes] = await Promise.allSettled([
        homeApi.getPublicRoutes(date, limit),
        homeApi.getPopularRoutes(date, limit),
        homeApi.getRouteCollections(),
        homeApi.getUpcomingEvents(date, dateTo, limit),
        homeApi.getRecommendedRoutes(date, limit),
      ]);

      const result = emptyHomePageData();

      if (publicRes.status === 'fulfilled') {
        result.publicRoutes = await withStepsCount(publicRes.value.map(mapRouteDtoToHomeRouteCard));
      }

      if (popularRes.status === 'fulfilled') {
        result.popularRoutes = await withStepsCount(popularRes.value.map(mapRouteDtoToHomeRouteCard));
      }

      if (collectionsRes.status === 'fulfilled') {
        result.collections = collectionsRes.value.map(mapRouteCollectionDtoToModel);
      }

      if (eventsRes.status === 'fulfilled') {
        result.upcomingEvents = eventsRes.value.map(mapEventDtoToEventCard);
      }

      if (recommendedRes.status === 'fulfilled') {
        result.recommendedRoutes = await withStepsCount(
          recommendedRes.value.map(mapRouteDtoToHomeRouteCard),
        );
      } else {
        result.recommendationError = isAuthenticated;
      }

      return result;
    },
  });
}
