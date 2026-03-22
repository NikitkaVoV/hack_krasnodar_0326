import type { RouteDto } from '@/entities/route/dto';
import type { EventDto } from '@/entities/event/dto';

export interface RouteCollectionDto {
  id: string;
  title: string;
  description?: string;
  key?: string;
}

export interface HomeAggregateDto {
  publicRoutes?: RouteDto[];
  popularRoutes?: RouteDto[];
  recommendedRoutes?: RouteDto[];
  upcomingEvents?: EventDto[];
  collections?: RouteCollectionDto[];
}


