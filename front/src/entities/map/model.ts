export type MapEntityType = 'place' | 'event';
export type EntityTypeFilter = 'places' | 'events' | 'both';

export interface MapPointSelection {
  lat: number;
  lng: number;
  radiusKm: number;
}

export interface BaseMapItem {
  id: string;
  type: MapEntityType;
  title: string;
  description: string;
  lat: number;
  lng: number;
  distanceKm: number;
  category: string;
  tags: string[];
  imageUrl?: string;
  popularity: number;
  isOpenNow: boolean;
  isFavorite: boolean;
  badges: Array<'popular' | 'new' | 'hidden-gem' | 'open-now' | 'event-ongoing'>;
}

export interface PlaceMapItem extends BaseMapItem {
  type: 'place';
  practicalInfo: string;
  avgBudget?: number;
  avgDurationMinutes?: number;
}

export interface EventMapItem extends BaseMapItem {
  type: 'event';
  eventStartAt: string;
  eventEndAt?: string;
}

export type MapEntity = PlaceMapItem | EventMapItem;

export interface FilterState {
  quickFilters: string[];
  entityType: EntityTypeFilter;
  minBudget?: number;
  maxBudget?: number;
  duration?: 'short' | 'half-day' | 'full-day' | 'any';
  date?: string;
  time?: string;
  openNow: boolean;
  suitableFor: Array<'children' | 'elderly' | 'solo' | 'groups'>;
  sortBy: 'distance' | 'popularity' | 'relevance' | 'starting-soon';
}

export interface MapSearchRequest {
  lat: number;
  lng: number;
  radiusKm: number;
  filter: FilterState;
}

export interface MapSearchResponse {
  items: MapEntity[];
  total: number;
}
