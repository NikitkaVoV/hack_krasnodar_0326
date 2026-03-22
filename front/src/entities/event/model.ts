export interface EventCardModel {
  id: string;
  name: string;
  description: string;
  location: string;
  startAt?: string;
}

export interface RouteTargetModel {
  id: string;
  type: 'place' | 'event';
  name: string;
  description: string;
  location: string;
  startAt?: string;
}


