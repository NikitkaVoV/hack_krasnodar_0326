export interface AppRoute {
  id: string;
  userId: string;
  date: string;
  totalDurationMinutes: number;
  summary: string;
  advice: string;
}

export interface AppRouteStep {
  targetId: string;
  targetType: 'place' | 'event' | 'unknown';
  stepOrder: number;
  plannedTimeStart: string;
  plannedTimeEnd: string;
  travelTimeMinutes: number;
  waitTimeMinutes: number;
  notes: string;
  transportMode: string;
  priority: number;
}


