import placeholderImage from '@/assets/placeholder.png';
import { routesApi } from '@/shared/api/routesApi';
import { eventsApi } from '@/shared/api/eventsApi';
import { placesApi } from '@/shared/api/placesApi';
import { mapRouteStepDtoToAppRouteStep } from '@/entities/route/mapper';
import type { RouteDto, RouteStepDto } from '@/entities/route/dto';
import type { AppRouteStep } from '@/entities/route/model';
import type { PublicRouteCard, PublicRouteDetails, PublicRoutePoint } from '@/entities/publicRoute/model';

const KRASNODAR_CENTER: [number, number] = [45.03547, 38.97531];

function readString(record: Record<string, unknown>, keys: string[], fallback = ''): string {
  for (const key of keys) {
    const value = record[key];
    if (typeof value === 'string' && value.trim()) {
      return value;
    }
  }
  return fallback;
}

function readNumber(record: Record<string, unknown>, keys: string[]): number | null {
  for (const key of keys) {
    const value = record[key];
    if (typeof value === 'number' && Number.isFinite(value)) {
      return value;
    }
  }
  return null;
}

function readStringArray(record: Record<string, unknown>, keys: string[]): string[] {
  for (const key of keys) {
    const value = record[key];
    if (Array.isArray(value)) {
      return value.filter((item): item is string => typeof item === 'string');
    }
  }
  return [];
}

function readImageUrl(record: Record<string, unknown>): string | undefined {
  const direct = readString(
    record,
    ['imageUrl', 'coverImage', 'image', 'previewImage', 'image_url', 'cover_image'],
    '',
  );
  if (direct) {
    return direct;
  }

  const photos = record.photos;
  if (Array.isArray(photos)) {
    for (const photo of photos) {
      if (typeof photo === 'string' && photo.trim()) {
        return photo;
      }
      if (photo && typeof photo === 'object') {
        const url = readString(photo as Record<string, unknown>, ['url', 'src', 'imageUrl', 'image'], '');
        if (url) {
          return url;
        }
      }
    }
  }

  const images = record.images;
  if (Array.isArray(images)) {
    for (const image of images) {
      if (typeof image === 'string' && image.trim()) {
        return image;
      }
      if (image && typeof image === 'object') {
        const url = readString(image as Record<string, unknown>, ['url', 'src', 'imageUrl', 'image'], '');
        if (url) {
          return url;
        }
      }
    }
  }

  return undefined;
}

function inferCategory(tags: string[]): string {
  if (tags.includes('nature')) return 'nature';
  if (tags.includes('gastro')) return 'gastro';
  if (tags.includes('culture')) return 'culture';
  return 'city';
}

function inferBadges(route: RouteDto, tags: string[]): PublicRouteCard['badges'] {
  const source = route as unknown as Record<string, unknown>;
  const raw = readStringArray(source, ['badges']);
  if (raw.length) {
    return raw.filter(
      (item): item is PublicRouteCard['badges'][number] =>
        item === 'popular' || item === 'new' || item === 'family' || item === 'weekend',
    );
  }

  const badges: PublicRouteCard['badges'] = [];
  if (tags.includes('family')) badges.push('family');
  if (tags.includes('weekend')) badges.push('weekend');
  if (route.totalDuration > 360) badges.push('popular');
  if (!badges.length) badges.push('new');
  return badges;
}

async function getPublicRoutesSource(date: string): Promise<RouteDto[]> {
  try {
    return await routesApi.getRoutesForUserAndDate('public', date);
  } catch {
    return routesApi.getRoutesForUserAndDate('', date).catch(() => []);
  }
}

async function countRoutePoints(routeId: string): Promise<number> {
  try {
    const points = await routesApi.getRouteSteps(routeId);
    return points.length;
  } catch {
    return 0;
  }
}

function fallbackCoordinates(order: number): { lat: number; lng: number } {
  const angle = (order * Math.PI) / 3;
  const radius = 0.007 + order * 0.0015;
  return {
    lat: KRASNODAR_CENTER[0] + Math.sin(angle) * radius,
    lng: KRASNODAR_CENTER[1] + Math.cos(angle) * radius,
  };
}

async function resolvePointDetails(step: AppRouteStep, rawStep: RouteStepDto): Promise<Partial<PublicRoutePoint>> {
  const raw = rawStep as unknown as Record<string, unknown>;

  const lat = readNumber(raw, ['lat', 'latitude']);
  const lng = readNumber(raw, ['lng', 'lon', 'longitude']);

  if (step.targetType === 'place') {
    try {
      const placeRaw = (await placesApi.getPlaceById(step.targetId)) as unknown as Record<string, unknown>;
      return {
        title: readString(placeRaw, ['name', 'title'], 'Локация маршрута'),
        description: readString(placeRaw, ['description'], 'Описание точки недоступно.'),
        imageUrl: readImageUrl(placeRaw),
        placeId: readString(placeRaw, ['id'], step.targetId),
        location: readString(placeRaw, ['location', 'address']),
        lat: lat ?? readNumber(placeRaw, ['lat', 'latitude']) ?? undefined,
        lng: lng ?? readNumber(placeRaw, ['lng', 'lon', 'longitude']) ?? undefined,
      };
    } catch {
      return {
        title: 'Локация маршрута',
        description: 'Описание точки недоступно.',
        lat: lat ?? undefined,
        lng: lng ?? undefined,
      };
    }
  }

  if (step.targetType === 'event') {
    try {
      const eventRaw = (await eventsApi.getEventById(step.targetId)) as unknown as Record<string, unknown>;
      return {
        title: readString(eventRaw, ['name', 'title'], 'Событие'),
        description: readString(eventRaw, ['description'], 'Описание события недоступно.'),
        imageUrl: readImageUrl(eventRaw),
        location: readString(eventRaw, ['location', 'address']),
        lat: lat ?? readNumber(eventRaw, ['lat', 'latitude']) ?? undefined,
        lng: lng ?? readNumber(eventRaw, ['lng', 'lon', 'longitude']) ?? undefined,
      };
    } catch {
      return {
        title: 'Событие',
        description: 'Описание события недоступно.',
        lat: lat ?? undefined,
        lng: lng ?? undefined,
      };
    }
  }

  return {
    title: 'Точка маршрута',
    description: 'Описание точки недоступно.',
    lat: lat ?? undefined,
    lng: lng ?? undefined,
  };
}

function buildCard(route: RouteDto, pointsCount: number): PublicRouteCard {
  const source = route as unknown as Record<string, unknown>;
  const tags = readStringArray(source, ['tags']);
  const suitableFor = readStringArray(source, ['suitableFor']);

  return {
    id: route.id,
    title: readString(source, ['title', 'name'], route.summary),
    description: readString(source, ['description'], route.advice || 'Готовый маршрут от платформы.'),
    imageUrl: readImageUrl(source) ?? placeholderImage,
    durationMinutes: route.totalDuration,
    distanceKm: readNumber(source, ['distanceKm', 'distance']),
    estimatedBudget: readNumber(source, ['estimatedBudget', 'budget']),
    tags,
    suitableFor,
    badges: inferBadges(route, tags),
    pointsCount,
    category: readString(source, ['category'], inferCategory(tags)),
  };
}

export function mapRouteDtoToPublicRouteCard(
  route: RouteDto,
  pointsCount = 0,
  imageUrlOverride?: string,
): PublicRouteCard {
  const base = buildCard(route, pointsCount);
  return {
    ...base,
    imageUrl: imageUrlOverride || base.imageUrl,
  };
}

export async function resolveRoutePreviewImage(routeId: string): Promise<string | undefined> {
  try {
    const firstStep = (await routesApi.getRouteSteps(routeId))
      .map(mapRouteStepDtoToAppRouteStep)
      .sort((a, b) => a.stepOrder - b.stepOrder)[0];

    if (!firstStep) {
      return undefined;
    }

    if (firstStep.targetType === 'place') {
      const placeRaw = (await placesApi.getPlaceById(firstStep.targetId)) as unknown as Record<string, unknown>;
      return readImageUrl(placeRaw);
    }

    if (firstStep.targetType === 'event') {
      const eventRaw = (await eventsApi.getEventById(firstStep.targetId)) as unknown as Record<string, unknown>;
      return readImageUrl(eventRaw);
    }
  } catch {
    return undefined;
  }

  return undefined;
}

export const publicRoutesApi = {
  async getPublicRoutesCatalog(limit = 24, date = new Date().toISOString().slice(0, 10)): Promise<PublicRouteCard[]> {
    const routes = await getPublicRoutesSource(date);
    const limitedRoutes = routes.slice(0, limit);

    const withCounts = await Promise.all(
      limitedRoutes.map(async (route) => ({
        route,
        pointsCount: await countRoutePoints(route.id),
        previewImage: await resolveRoutePreviewImage(route.id),
      })),
    );

    return withCounts.map((item) =>
      mapRouteDtoToPublicRouteCard(item.route, item.pointsCount, item.previewImage),
    );
  },

  async getPublicRouteDetails(routeId: string): Promise<PublicRouteDetails> {
    const [routeDto, stepsDto] = await Promise.all([
      routesApi.getRouteById(routeId),
      routesApi.getRouteSteps(routeId),
    ]);

    const base = buildCard(routeDto, stepsDto.length);
    const mappedSteps = stepsDto
      .map(mapRouteStepDtoToAppRouteStep)
      .map((step, index) => ({ step, raw: stepsDto[index] }))
      .sort((a, b) => a.step.stepOrder - b.step.stepOrder);

    const pointsRaw = await Promise.all(
      mappedSteps.map(async ({ step, raw }, index) => {
        const resolved = await resolvePointDetails(step, raw);
        const fallback = fallbackCoordinates(index + 1);

        return {
          id: `${routeId}-${step.stepOrder}-${step.targetId}`,
          order: step.stepOrder,
          title: resolved.title ?? `Точка ${step.stepOrder}`,
          description: resolved.description ?? 'Описание точки недоступно.',
          lat: typeof resolved.lat === 'number' ? resolved.lat : fallback.lat,
          lng: typeof resolved.lng === 'number' ? resolved.lng : fallback.lng,
          imageUrl: resolved.imageUrl || undefined,
          placeId: resolved.placeId,
          estimatedStopMinutes: step.waitTimeMinutes || undefined,
          type: step.targetType,
          location: resolved.location,
        } satisfies PublicRoutePoint;
      }),
    );

    return {
      ...base,
      points: pointsRaw,
    };
  },
};
