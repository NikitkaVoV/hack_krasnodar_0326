export type BuilderTransportMode = 'walk' | 'car' | 'mixed';
export type BuilderPacePreset = 'calm' | 'active' | 'family' | 'gastro' | 'mixed';

export interface RouteBuilderSettings {
  date: string;
  startTime: string;
  totalDurationMinutes: number;
  radiusKm: number;
  transportMode: BuilderTransportMode;
  pacePreset: BuilderPacePreset;
  onlyOpenPlaces: boolean;
  includeEvents: boolean;
  manualSearch: string;
}

export type RouteBuilderRecommendationKind = 'start' | 'next' | 'events' | 'alternative';

export interface RouteBuilderRecommendation {
  id: string;
  sourceId: string;
  kind: RouteBuilderRecommendationKind;
  type: 'place' | 'event';
  title: string;
  description: string;
  lat: number;
  lng: number;
  imageUrl?: string;
  category: string;
  tags: string[];
  estimatedVisitMinutes: number;
  travelMinutesFromLast: number;
  distanceKm: number;
  popularity: number;
  isOpenNow: boolean;
  eventStartAt?: string;
  eventEndAt?: string;
  statusLabel: string;
}

export interface RouteBuilderStep extends RouteBuilderRecommendation {
  order: number;
  plannedStartAt: string;
  plannedEndAt: string;
  waitMinutes: number;
  note?: string;
  isPinned?: boolean;
}

export interface RouteBuilderWarning {
  id: string;
  level: 'warning' | 'critical';
  title: string;
  message: string;
  stepOrder?: number;
}

export interface RouteBuilderSummary {
  stepsCount: number;
  totalVisitMinutes: number;
  totalTravelMinutes: number;
  totalWaitMinutes: number;
  totalPlannedMinutes: number;
  remainingMinutes: number;
  status: 'ok' | 'tight' | 'overloaded';
}

export interface RouteBuilderRecommendationGroup {
  id: RouteBuilderRecommendationKind;
  title: string;
  subtitle: string;
  items: RouteBuilderRecommendation[];
  emptyTitle: string;
  emptyDescription: string;
}

export interface RouteBuilderUserContext {
  userType?: string;
  age?: number;
  budgetMin?: number;
  budgetMax?: number;
  lastLocation?: { lat: number; lng: number };
  preferenceTags: string[];
  constraintTags: string[];
  interestTags: string[];
}

