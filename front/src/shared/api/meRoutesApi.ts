import { http } from '@/shared/api/http';
import type { RouteDto } from '@/entities/route/dto';
import type { PublicRouteCard } from '@/entities/publicRoute/model';
import { mapRouteDtoToPublicRouteCard, resolveRoutePreviewImage } from '@/shared/api/publicRoutesApi';

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

export const meRoutesApi = {
  async getMyRoutes(): Promise<PublicRouteCard[]> {
    const { data } = await http.get<RouteDto[] | RoutesListResponseDto>('/api/me/routes');
    const routes = normalizeRoutesListResponse(data);
    const withPreview = await Promise.all(
      routes.map(async (route) => ({
        route,
        previewImage: await resolveRoutePreviewImage(route.id),
      })),
    );

    return withPreview.map((item) => mapRouteDtoToPublicRouteCard(item.route, 0, item.previewImage));
  },

  async deleteMyRoute(routeId: string): Promise<void> {
    await http.delete(`/api/me/routes/${routeId}`);
  },
};
