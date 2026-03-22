import { formatMinutes } from '@/shared/lib/format';
import type { RoutePdfAlternative, RoutePdfModel, RoutePdfSource, RoutePdfStepModel } from './types';
import { findStepByOrder } from './collectRoutePdfSource';

function readString(record: Record<string, unknown>, keys: string[], fallback = ''): string {
  for (const key of keys) {
    const value = record[key];
    if (typeof value === 'string' && value.trim()) {
      return value;
    }
  }
  return fallback;
}

function readAlternatives(record: Record<string, unknown>): RoutePdfAlternative[] {
  const candidates = ['alternatives', 'alternativePoints', 'extraPoints'];

  for (const key of candidates) {
    const value = record[key];
    if (!Array.isArray(value)) {
      continue;
    }

    return value
      .map((item): RoutePdfAlternative | null => {
        if (typeof item === 'string') {
          const text = item.trim();
          return text ? { title: text } : null;
        }

        if (item && typeof item === 'object') {
          const source = item as Record<string, unknown>;
          const title = readString(source, ['title', 'name', 'label']);
          const description = readString(source, ['description', 'summary'], '');
          if (!title) {
            return null;
          }
          return {
            title,
            description: description || undefined,
          };
        }

        return null;
      })
      .filter((item): item is RoutePdfAlternative => Boolean(item));
  }

  return [];
}

function formatDate(date?: string): string | undefined {
  if (!date) {
    return undefined;
  }

  const parsed = Date.parse(date);
  if (!Number.isFinite(parsed)) {
    return date;
  }

  return new Date(parsed).toLocaleString('ru-RU', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
}

function toStepDurationMinutes(start?: string, end?: string): number | undefined {
  if (!start || !end) {
    return undefined;
  }

  const startTs = Date.parse(start);
  const endTs = Date.parse(end);

  if (!Number.isFinite(startTs) || !Number.isFinite(endTs) || endTs < startTs) {
    return undefined;
  }

  return Math.round((endTs - startTs) / 60000);
}

function stringValue(value: string | number | null | undefined): string {
  if (value === null || value === undefined || value === '') {
    return '—';
  }
  return String(value);
}

function formatCoordinates(lat?: number, lng?: number): string {
  if (typeof lat !== 'number' || typeof lng !== 'number') {
    return '—';
  }
  return `${lat.toFixed(5)}, ${lng.toFixed(5)}`;
}

export function mapRoutePdfSourceToModel(source: RoutePdfSource): RoutePdfModel {
  const routeRecord = source.routeDto as unknown as Record<string, unknown>;
  const title = source.publicDetails.title || source.routeDto.summary || `Маршрут ${source.routeId}`;

  const steps: RoutePdfStepModel[] = source.publicDetails.points
    .slice()
    .sort((a, b) => a.order - b.order)
    .map((point, index) => {
      const step = findStepByOrder(source.stepsDto, point.order);
      const place = step ? source.placeByStep[step.targetId] : null;
      const event = step ? source.eventByStep[step.targetId] : null;

      const description = point.description || place?.description || event?.description;
      const address = point.location || place?.location || event?.location;

      return {
        order: point.order || index + 1,
        title: point.title || place?.name || event?.name || `Точка ${index + 1}`,
        type: point.type,
        description: description || undefined,
        address: address || undefined,
        lat: point.lat,
        lng: point.lng,
        durationMinutes:
          point.estimatedStopMinutes ?? toStepDurationMinutes(step?.plannedTimeStart, step?.plannedTimeEnd),
        plannedStart: formatDate(step?.plannedTimeStart),
        plannedEnd: formatDate(step?.plannedTimeEnd),
        travelMinutes: step?.travelTimeMinutes,
        waitMinutes: step?.waitTimeMinutes,
        notes: step?.notes || undefined,
        imageUrl: point.imageUrl || place?.imageUrl || event?.imageUrl,
        place: place
          ? {
              id: place.id,
              name: place.name,
              description: place.description,
              location: place.location,
              imageUrl: place.imageUrl,
            }
          : null,
        event: event
          ? {
              id: event.id,
              name: event.name,
              description: event.description,
              location: event.location,
              startAt: event.startAt,
              imageUrl: event.imageUrl,
            }
          : null,
      };
    });

  const usefulFields: Array<{ label: string; value: string }> = [
    { label: 'ID маршрута', value: source.routeId },
    { label: 'Дата маршрута', value: stringValue(source.routeDto.date) },
    { label: 'Длительность', value: formatMinutes(source.publicDetails.durationMinutes) },
    {
      label: 'Дистанция',
      value:
        typeof source.publicDetails.distanceKm === 'number'
          ? `${source.publicDetails.distanceKm.toFixed(1)} км`
          : '—',
    },
    {
      label: 'Бюджет',
      value:
        typeof source.publicDetails.estimatedBudget === 'number'
          ? `${Math.round(source.publicDetails.estimatedBudget).toLocaleString('ru-RU')} ₽`
          : '—',
    },
    {
      label: 'Координаты первой точки',
      value: steps.length ? formatCoordinates(steps[0].lat, steps[0].lng) : '—',
    },
  ];

  return {
    routeId: source.routeId,
    title,
    summary: source.routeDto.summary || undefined,
    description: source.publicDetails.description || undefined,
    date: formatDate(source.routeDto.date),
    durationMinutes: source.publicDetails.durationMinutes,
    distanceKm: source.publicDetails.distanceKm,
    estimatedBudget: source.publicDetails.estimatedBudget,
    advice: source.routeDto.advice || undefined,
    author: source.routeDto.user || undefined,
    coverImageUrl: source.publicDetails.imageUrl,
    exportedAt: new Date().toISOString(),
    tags: [...source.publicDetails.tags, ...source.publicDetails.badges],
    usefulFields,
    steps,
    alternatives: readAlternatives(routeRecord),
  };
}

export function toPdfFileName(model: RoutePdfModel): string {
  const safeRouteId = model.routeId?.trim();

  if (safeRouteId) {
    return `route-${safeRouteId}.pdf`;
  }

  const slug = model.title
    .toLowerCase()
    .replace(/[^a-z0-9а-яё]+/gi, '-')
    .replace(/^-+|-+$/g, '')
    .slice(0, 48);

  return `route-${slug || 'export'}.pdf`;
}
