import type { RouteDto } from '@/entities/route/dto';
import type { EventDto } from '@/entities/event/dto';
import type { RouteCollectionDto } from './dto';
import type { EventCard, HomeRouteCard, RouteCollection } from './model';
import { mapRouteDtoToAppRoute } from '@/entities/route/mapper';

export function mapRouteDtoToHomeRouteCard(dto: RouteDto): HomeRouteCard {
  const route = mapRouteDtoToAppRoute(dto);

  return {
    id: route.id,
    summary: route.summary,
    advice: route.advice,
    date: route.date,
    totalDurationMinutes: route.totalDurationMinutes,
  };
}

export function mapEventDtoToEventCard(dto: EventDto): EventCard {
  return {
    id: dto.id,
    name: dto.name ?? 'Событие',
    description: dto.description ?? 'Описание появится позже.',
    location: dto.location ?? 'Локация уточняется',
    startAt: dto.startAt,
  };
}

export function mapRouteCollectionDtoToModel(dto: RouteCollectionDto): RouteCollection {
  return {
    id: dto.id,
    title: dto.title,
    description: dto.description ?? 'Подборка маршрутов по теме.',
    key: dto.key ?? dto.title,
  };
}
