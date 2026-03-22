import { useQuery } from '@tanstack/react-query';
import { routesApi } from '@/shared/api/routesApi';
import { mapRouteDtoToAppRoute, mapRouteStepDtoToAppRouteStep } from '@/entities/route/mapper';
import { eventsApi } from '@/shared/api/eventsApi';
import { placesApi } from '@/shared/api/placesApi';
import { mapEventDtoToEventCardModel } from '@/entities/event/mapper';
import { mapPlaceDtoToPlaceCardModel } from '@/entities/place/mapper';
import type { RouteTargetModel } from '@/entities/event/model';

async function resolveTarget(
  targetType: 'event' | 'place' | 'unknown',
  targetId: string,
): Promise<RouteTargetModel | null> {
  if (targetType === 'place') {
    const place = mapPlaceDtoToPlaceCardModel(await placesApi.getPlaceById(targetId));
    return { ...place, type: 'place' };
  }
  if (targetType === 'event') {
    const event = mapEventDtoToEventCardModel(await eventsApi.getEventById(targetId));
    return { ...event, type: 'event' };
  }
  return null;
}

export function useRouteDetailsQuery(routeId: string) {
  return useQuery({
    queryKey: ['route-details', routeId],
    queryFn: async () => {
      const [routeDto, stepsDto] = await Promise.all([
        routesApi.getRouteById(routeId),
        routesApi.getRouteSteps(routeId),
      ]);

      const route = mapRouteDtoToAppRoute(routeDto);
      const steps = stepsDto
        .map(mapRouteStepDtoToAppRouteStep)
        .sort((a, b) => a.stepOrder - b.stepOrder);

      const targetsList = await Promise.all(
        steps.map((step) => resolveTarget(step.targetType, step.targetId)),
      );

      const targets = new Map<string, RouteTargetModel>();
      targetsList.forEach((target) => {
        if (target) {
          targets.set(target.id, target);
        }
      });

      return { route, steps, targets };
    },
  });
}

