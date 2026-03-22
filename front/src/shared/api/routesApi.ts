import { http } from '@/shared/api/http';
import type { RouteDto, RouteStepDto } from '@/entities/route/dto';

interface RoutesListResponseDto {
  items?: RouteDto[];
  total?: number;
}

function normalizeRoutesListResponse(data: RouteDto[] | RoutesListResponseDto): RouteDto[] {
  if (Array.isArray(data)) {
    return data;
  }

  if (Array.isArray(data.items)) {
    return data.items;
  }

  return [];
}

export const routesApi = {
  async getRoutesForUserAndDate(userId: string, date: string): Promise<RouteDto[]> {
    const { data } = await http.get<RouteDto[] | RoutesListResponseDto>('/api/routes/list', {
      params: { userId, date },
    });
    return normalizeRoutesListResponse(data);
  },

  async getRouteById(routeId: string): Promise<RouteDto> {
    const { data } = await http.get<RouteDto>(`/api/routes/list/${routeId}`);
    return data;
  },

  async getRouteSteps(routeId: string): Promise<RouteStepDto[]> {
    const { data } = await http.get<RouteStepDto[]>('/api/route-steps', {
      params: { routeId },
    });
    return data;
  },
};

