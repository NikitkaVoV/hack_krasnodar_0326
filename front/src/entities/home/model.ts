export interface HomeRouteCard {
  id: string;
  summary: string;
  date: string;
  totalDurationMinutes: number;
  advice: string;
  stepsCount?: number;
}

export interface EventCard {
  id: string;
  name: string;
  description: string;
  location: string;
  startAt?: string;
}

export type HomeEventCard = EventCard;

export interface RouteCollection {
  id: string;
  title: string;
  description: string;
  key: string;
}
