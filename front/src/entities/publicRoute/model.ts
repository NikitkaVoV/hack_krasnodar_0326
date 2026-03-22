export type PublicRouteBadge = 'popular' | 'new' | 'family' | 'weekend';

export interface PublicRouteCard {
  id: string;
  title: string;
  description: string;
  imageUrl?: string;
  durationMinutes: number;
  distanceKm: number | null;
  estimatedBudget: number | null;
  badges: PublicRouteBadge[];
  tags: string[];
  suitableFor: string[];
  pointsCount: number;
  category: string;
}

export interface PublicRoutePoint {
  id: string;
  order: number;
  title: string;
  description: string;
  lat: number;
  lng: number;
  imageUrl?: string;
  placeId?: string;
  estimatedStopMinutes?: number;
  type: 'place' | 'event' | 'unknown';
  location?: string;
}

export interface PublicRouteDetails extends PublicRouteCard {
  points: PublicRoutePoint[];
}

export interface PublicRoutesFilterState {
  searchQuery: string;
  date: string;
  quickTags: string[];
  duration: 'any' | 'short' | 'medium' | 'long';
  budget: 'any' | 'low' | 'medium' | 'high';
  category: 'all' | 'city' | 'nature' | 'gastro' | 'culture';
  suitableFor: 'all' | 'solo' | 'family' | 'groups';
  sortBy: 'popular' | 'duration' | 'distance' | 'budget';
}
