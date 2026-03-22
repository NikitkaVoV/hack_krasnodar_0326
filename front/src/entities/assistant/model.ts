export type UUID = string;

export type AssistantMessageRequest = {
  sessionId: UUID | null;
  message: string;
  clientContext?: {
    routeId: UUID | null;
    placeId: UUID | null;
    eventId: UUID | null;
  } | null;
};

export type AssistantRouteStepForSave = {
  id: UUID;
  type: 'place' | 'event';
  title?: string;
  description?: string;
  address?: string;
  lat?: number;
  lng?: number;
  duration?: number;
  orderIndex?: number;
  imageUrl?: string;
  plannedTimeStart?: string;
  plannedTimeEnd?: string;
};

export type AssistantRouteForSave = {
  summary?: string;
  advice?: string;
  totalDuration?: number;
  date?: string;
  imageUrl?: string;
  canBeSaved?: boolean;
  steps: AssistantRouteStepForSave[];
};

export type SaveRouteFromAiRequest = {
  route: AssistantRouteForSave;
};

export type SaveRouteFromAiResponse = {
  routeId: UUID;
  message: string;
};

export type AssistantMessageResponse = {
  sessionId: UUID;
  reply: { text: string; tone: string | null };
  normalizedRequest: {
    requestId: UUID;
    sessionId: UUID;
    userId: UUID;
    intent: string;
    subIntent: string;
    entities: {
      date?: string | null;
      dateFrom?: string | null;
      dateTo?: string | null;
      groupType?: string | null;
      pace?: string | null;
      budgetLevel?: string | null;
      transportMode?: string | null;
      locationText?: string | null;
      lat?: number | null;
      lng?: number | null;
      tags: string[];
      categories: string[];
      constraints: string[];
    };
    references: {
      routeId: UUID | null;
      placeId: UUID | null;
      eventId: UUID | null;
    };
    missingFields: string[];
    readyForExecution: boolean;
    originalMessage: string;
  };
  systemResponse: {
    status: string;
    responseType: string;
    summary: string;
    payload: unknown;
    advice: string[];
    warnings: string[];
    nextActions: string[];
  };
  debug?: {
    detectedIntent?: string | null;
    resolutionStage?: string | null;
    handlerName?: string | null;
  } | null;
};
