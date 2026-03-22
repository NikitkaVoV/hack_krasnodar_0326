import { routesApi } from '@/shared/api/routesApi';
import { placesApi } from '@/shared/api/placesApi';
import { eventsApi } from '@/shared/api/eventsApi';
import type { PublicRouteCard, PublicRouteDetails, PublicRoutePoint } from '@/entities/publicRoute/model';
import type { EventDto } from '@/entities/event/dto';
import type { PlaceDto } from '@/entities/place/dto';
import type { RouteDto, RouteStepDto } from '@/entities/route/dto';
import type { RoutePdfSource } from './types';

const KRASNODAR_CENTER: [number, number] = [45.03547, 38.97531];

function normalizeTargetType(value: string): 'place' | 'event' | 'unknown' {
  if (value === 'place' || value === 'event') {
    return value;
  }
  return 'unknown';
}

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
      return value.filter((item): item is string => typeof item === 'string' && item.trim().length > 0);
    }
  }
  return [];
}

function readImageUrl(record: Record<string, unknown>): string | undefined {
  const direct = readString(record, ['imageUrl', 'coverImage', 'image', 'previewImage', 'image_url', 'cover_image']);
  if (direct) {
    return direct;
  }

  const arrays = [record.photos, record.images];

  for (const entry of arrays) {
    if (!Array.isArray(entry)) {
      continue;
    }

    for (const candidate of entry) {
      if (typeof candidate === 'string' && candidate.trim()) {
        return candidate;
      }

      if (candidate && typeof candidate === 'object') {
        const url = readString(candidate as Record<string, unknown>, ['url', 'src', 'imageUrl', 'image']);
        if (url) {
          return url;
        }
      }
    }
  }

  return undefined;
}

function fallbackCoordinates(order: number): { lat: number; lng: number } {
  const angle = (order * Math.PI) / 3;
  const radius = 0.007 + order * 0.0015;
  return {
    lat: KRASNODAR_CENTER[0] + Math.sin(angle) * radius,
    lng: KRASNODAR_CENTER[1] + Math.cos(angle) * radius,
  };
}

function withTimeout<T>(promise: Promise<T>, timeoutMs: number, message: string): Promise<T> {
  return new Promise<T>((resolve, reject) => {
    const timer = window.setTimeout(() => reject(new Error(message)), timeoutMs);

    promise
      .then((value) => {
        window.clearTimeout(timer);
        resolve(value);
      })
      .catch((error) => {
        window.clearTimeout(timer);
        reject(error);
      });
  });
}

async function safeFetch<T>(fn: () => Promise<T>, timeoutMs: number): Promise<T | null> {
  try {
    return await withTimeout(fn(), timeoutMs, 'Истекло время ожидания загрузки данных точки маршрута.');
  } catch {
    return null;
  }
}

function inferBadges(routeRecord: Record<string, unknown>): PublicRouteCard['badges'] {
  const raw = readStringArray(routeRecord, ['badges']);
  if (!raw.length) {
    return [];
  }

  return raw.filter(
    (item): item is PublicRouteCard['badges'][number] =>
      item === 'popular' || item === 'new' || item === 'family' || item === 'weekend',
  );
}

function buildPoint(step: RouteStepDto, orderIndex: number, place: PlaceDto | null, event: EventDto | null): PublicRoutePoint {
  const stepRaw = step as unknown as Record<string, unknown>;
  const latRaw = readNumber(stepRaw, ['lat', 'latitude']);
  const lngRaw = readNumber(stepRaw, ['lng', 'lon', 'longitude']);

  const fallback = fallbackCoordinates(orderIndex + 1);

  const lat =
    latRaw ??
    (place ? readNumber(place as unknown as Record<string, unknown>, ['lat', 'latitude']) : null) ??
    (event ? readNumber(event as unknown as Record<string, unknown>, ['lat', 'latitude']) : null) ??
    fallback.lat;

  const lng =
    lngRaw ??
    (place ? readNumber(place as unknown as Record<string, unknown>, ['lng', 'lon', 'longitude']) : null) ??
    (event ? readNumber(event as unknown as Record<string, unknown>, ['lng', 'lon', 'longitude']) : null) ??
    fallback.lng;

  const targetType = normalizeTargetType(step.targetType);

  const durationMinutes =
    Number.isFinite(Date.parse(step.plannedTimeStart)) && Number.isFinite(Date.parse(step.plannedTimeEnd))
      ? Math.max(1, Math.round((Date.parse(step.plannedTimeEnd) - Date.parse(step.plannedTimeStart)) / 60000))
      : undefined;

  const placeRaw = place as unknown as Record<string, unknown> | null;
  const eventRaw = event as unknown as Record<string, unknown> | null;

  return {
    id: `${step.targetId}-${step.stepOrder}`,
    order: step.stepOrder,
    title: place?.name || event?.name || `Точка ${step.stepOrder}`,
    description: place?.description || event?.description || step.notes || 'Описание точки маршрута отсутствует.',
    lat,
    lng,
    imageUrl: place?.imageUrl || event?.imageUrl || undefined,
    placeId: targetType === 'place' ? step.targetId : undefined,
    estimatedStopMinutes: durationMinutes,
    type: targetType,
    location: place?.location || event?.location || readString(stepRaw, ['location', 'address'], ''),
  };
}

export async function collectRoutePdfSource(routeId: string): Promise<RoutePdfSource> {
  const [routeDto, rawSteps] = await Promise.all([
    withTimeout(routesApi.getRouteById(routeId), 15000, 'Истекло время ожидания маршрута.'),
    withTimeout(routesApi.getRouteSteps(routeId), 15000, 'Истекло время ожидания шагов маршрута.'),
  ]);

  const stepsDto = [...rawSteps].sort((a, b) => a.stepOrder - b.stepOrder);

  const uniquePlaceIds = Array.from(
    new Set(
      stepsDto
        .filter((step) => normalizeTargetType(step.targetType) === 'place')
        .map((step) => step.targetId),
    ),
  );

  const uniqueEventIds = Array.from(
    new Set(
      stepsDto
        .filter((step) => normalizeTargetType(step.targetType) === 'event')
        .map((step) => step.targetId),
    ),
  );

  const placeDetails = await Promise.all(
    uniquePlaceIds.map(async (id) => ({ id, value: await safeFetch(() => placesApi.getPlaceById(id), 8000) })),
  );

  const eventDetails = await Promise.all(
    uniqueEventIds.map(async (id) => ({ id, value: await safeFetch(() => eventsApi.getEventById(id), 8000) })),
  );

  const placeById = new Map<string, PlaceDto | null>();
  const eventById = new Map<string, EventDto | null>();

  placeDetails.forEach(({ id, value }) => placeById.set(id, value));
  eventDetails.forEach(({ id, value }) => eventById.set(id, value));

  const placeByStep: Record<string, PlaceDto | null> = {};
  const eventByStep: Record<string, EventDto | null> = {};

  stepsDto.forEach((step) => {
    const targetType = normalizeTargetType(step.targetType);
    placeByStep[step.targetId] = targetType === 'place' ? (placeById.get(step.targetId) ?? null) : null;
    eventByStep[step.targetId] = targetType === 'event' ? (eventById.get(step.targetId) ?? null) : null;
  });

  const points = stepsDto.map((step, index) =>
    buildPoint(step, index, placeByStep[step.targetId], eventByStep[step.targetId]),
  );

  const routeRecord = routeDto as unknown as Record<string, unknown>;
  const tags = readStringArray(routeRecord, ['tags']);
  const suitableFor = readStringArray(routeRecord, ['suitableFor']);

  const publicDetails: PublicRouteDetails = {
    id: routeId,
    title: readString(routeRecord, ['title', 'name'], routeDto.summary || `Маршрут ${routeId}`),
    description: readString(routeRecord, ['description'], routeDto.advice || 'Описание маршрута отсутствует.'),
    imageUrl: readImageUrl(routeRecord) || points.find((point) => point.imageUrl)?.imageUrl,
    durationMinutes: routeDto.totalDuration,
    distanceKm: readNumber(routeRecord, ['distanceKm', 'distance']),
    estimatedBudget: readNumber(routeRecord, ['estimatedBudget', 'budget']),
    badges: inferBadges(routeRecord),
    tags,
    suitableFor,
    pointsCount: points.length,
    category: readString(routeRecord, ['category'], 'city'),
    points,
  };

  return {
    routeId,
    publicDetails,
    routeDto,
    stepsDto,
    placeByStep,
    eventByStep,
  };
}

export function findStepByOrder(steps: RouteStepDto[], order: number): RouteStepDto | undefined {
  return steps.find((step) => step.stepOrder === order);
}
