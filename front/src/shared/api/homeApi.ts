import { http } from '@/shared/api/http';
import { routesApi } from '@/shared/api/routesApi';
import { eventsApi } from '@/shared/api/eventsApi';
import type { EventDto } from '@/entities/event/dto';
import type { RouteDto } from '@/entities/route/dto';
import type { HomeAggregateDto, RouteCollectionDto } from '@/entities/home/dto';

async function safeRequest<T>(request: () => Promise<T>): Promise<T | null> {
  try {
    return await request();
  } catch {
    return null;
  }
}

export const homeApi = {
  async getHomeAggregate(date: string, limit: number): Promise<HomeAggregateDto> {
    const { data } = await http.get<HomeAggregateDto>('/api/home', {
      params: { date, limit },
    });
    return data;
  },

  async getPublicRoutes(date: string, limit: number): Promise<RouteDto[]> {
    const { data } = await http.get<RouteDto[]>('/api/routes/public', {
      params: { date, limit },
    });
    return data;
  },

  async getPopularRoutes(date: string, limit: number): Promise<RouteDto[]> {
    const { data } = await http.get<RouteDto[]>('/api/routes/popular', {
      params: { date, limit },
    });
    return data;
  },

  async getRecommendedRoutes(date: string, limit: number): Promise<RouteDto[]> {
    const { data } = await http.get<RouteDto[]>('/api/routes/recommended', {
      params: { date, limit },
    });
    return data;
  },

  async getUpcomingEvents(dateFrom: string, dateTo: string, limit: number): Promise<EventDto[]> {
    const direct = await safeRequest(async () => {
      const { data } = await http.get<EventDto[]>('/api/events/upcoming', {
        params: { dateFrom, dateTo, limit },
      });
      return data;
    });
    if (direct) {
      return direct;
    }
    return eventsApi.getUpcomingEventsFallback(dateFrom, dateTo, limit);
  },

  async getRouteCollections(): Promise<RouteCollectionDto[]> {
    const { data } = await http.get<RouteCollectionDto[]>('/api/routes/collections');
    return data;
  },

  async getRouteStepsCount(routeId: string): Promise<number | undefined> {
    const steps = await safeRequest(() => routesApi.getRouteSteps(routeId));
    return steps?.length;
  },
};

