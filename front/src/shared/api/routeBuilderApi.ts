import { mapDiscoveryApi } from '@/shared/api/mapDiscoveryApi';
import { usersApi } from '@/shared/api/usersApi';
import type { FilterState, MapEntity, MapSearchRequest } from '@/entities/map/model';
import type { RouteBuilderSettings, RouteBuilderUserContext } from '@/entities/routeBuilder/model';

export interface RouteBuilderCandidatesRequest {
  lat: number;
  lng: number;
  settings: RouteBuilderSettings;
}

function toMapFilters(settings: RouteBuilderSettings): FilterState {
  return {
    quickFilters: [],
    entityType: settings.includeEvents ? 'both' : 'places',
    openNow: settings.onlyOpenPlaces,
    suitableFor: settings.pacePreset === 'family' ? ['children'] : [],
    sortBy: 'relevance',
    duration: settings.totalDurationMinutes <= 180 ? 'short' : settings.totalDurationMinutes <= 360 ? 'half-day' : 'any',
    date: settings.date,
    time: settings.startTime,
  };
}

function extractNonEmptyValue(value: unknown): string | null {
  if (typeof value === 'string') {
    const normalized = value.trim();
    return normalized.length ? normalized : null;
  }

  if (typeof value === 'number' && Number.isFinite(value)) {
    return String(value);
  }

  return null;
}

function unique(values: Array<string | null | undefined>): string[] {
  return Array.from(new Set(values.filter((item): item is string => Boolean(item))));
}

export const routeBuilderApi = {
  async getCandidates(request: RouteBuilderCandidatesRequest): Promise<MapEntity[]> {
    const payload: MapSearchRequest = {
      lat: request.lat,
      lng: request.lng,
      radiusKm: request.settings.radiusKm,
      filter: toMapFilters(request.settings),
    };

    const response = await mapDiscoveryApi.search(payload);

    if (!request.settings.manualSearch.trim()) {
      return response.items;
    }

    const searchValue = request.settings.manualSearch.trim().toLowerCase();

    return response.items.filter((item) => {
      const haystack = `${item.title} ${item.description} ${item.tags.join(' ')} ${item.category}`.toLowerCase();
      return haystack.includes(searchValue);
    });
  },

  async getUserContext(userId: string): Promise<RouteBuilderUserContext> {
    const safe = async <T>(fn: () => Promise<T>): Promise<T | null> => {
      try {
        return await fn();
      } catch {
        return null;
      }
    };

    const [user, preferences, tags, constraints] = await Promise.all([
      safe(() => usersApi.getUserById(userId)),
      safe(() => usersApi.getUserPreferences(userId)),
      safe(() => usersApi.getUserTags(userId)),
      safe(() => usersApi.getUserConstraints(userId)),
    ]);

    return {
      userType: user?.userType,
      age: user?.age,
      budgetMin: user?.budgetMin,
      budgetMax: user?.budgetMax,
      lastLocation:
        typeof user?.lastLocationLat === 'number' && typeof user?.lastLocationLng === 'number'
          ? { lat: user.lastLocationLat, lng: user.lastLocationLng }
          : undefined,
      preferenceTags: unique((preferences ?? []).map((item) => extractNonEmptyValue(item.preference))),
      interestTags: unique((tags ?? []).map((item) => extractNonEmptyValue(item.value ?? item.tagId))),
      constraintTags: unique((constraints ?? []).map((item) => extractNonEmptyValue(item.value ?? item.constraintId))),
    };
  },
};


