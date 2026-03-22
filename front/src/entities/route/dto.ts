export interface RouteDto {
  id: string;
  user: string;
  date: string;
  totalDuration: number;
  summary: string;
  advice: string;
  imageUrl?: string;
  coverImage?: string;
  previewImage?: string;
  photos?: Array<string | { url?: string; src?: string; imageUrl?: string }>;
  images?: Array<string | { url?: string; src?: string; imageUrl?: string }>;
}

export interface RouteStepDto {
  targetId: string;
  targetType: string;
  stepOrder: number;
  plannedTimeStart: string;
  plannedTimeEnd: string;
  travelTimeMinutes: number;
  waitTimeMinutes: number;
  notes: string;
  transportMode: string;
  priority: number;
}
