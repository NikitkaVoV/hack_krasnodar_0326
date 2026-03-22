import type { PublicRouteDetails } from '@/entities/publicRoute/model';
import type { RouteDto, RouteStepDto } from '@/entities/route/dto';
import type { EventDto } from '@/entities/event/dto';
import type { PlaceDto } from '@/entities/place/dto';

export interface RoutePdfStepModel {
  order: number;
  title: string;
  type: 'place' | 'event' | 'unknown';
  description?: string;
  address?: string;
  lat?: number;
  lng?: number;
  durationMinutes?: number;
  plannedStart?: string;
  plannedEnd?: string;
  travelMinutes?: number;
  waitMinutes?: number;
  notes?: string;
  imageUrl?: string;
  place?: Pick<PlaceDto, 'id' | 'name' | 'description' | 'location' | 'imageUrl'> | null;
  event?: Pick<EventDto, 'id' | 'name' | 'description' | 'location' | 'startAt' | 'imageUrl'> | null;
}

export interface RoutePdfAlternative {
  title: string;
  description?: string;
}

export interface RoutePdfModel {
  routeId: string;
  title: string;
  summary?: string;
  description?: string;
  date?: string;
  durationMinutes?: number;
  distanceKm?: number | null;
  estimatedBudget?: number | null;
  advice?: string;
  author?: string;
  coverImageUrl?: string;
  exportedAt: string;
  tags: string[];
  usefulFields: Array<{ label: string; value: string }>;
  steps: RoutePdfStepModel[];
  alternatives: RoutePdfAlternative[];
}

export interface RoutePdfSource {
  routeId: string;
  publicDetails: PublicRouteDetails;
  routeDto: RouteDto;
  stepsDto: RouteStepDto[];
  placeByStep: Record<string, PlaceDto | null>;
  eventByStep: Record<string, EventDto | null>;
}
