import axios from 'axios';
import { http } from '@/shared/api/http';
import { env } from '@/shared/config/env';
import type { MapEntity, MapSearchRequest, MapSearchResponse } from '@/entities/map/model';
import { searchMapEntitiesMock } from '@/shared/api/mock/mapDiscoveryMock';

interface RawMapSearchResponse {
  items?: MapEntity[];
  total?: number;
  places?: MapEntity[];
  events?: MapEntity[];
}

function normalizeResponse(raw: RawMapSearchResponse): MapSearchResponse {
  if (Array.isArray(raw.items)) {
    return {
      items: raw.items,
      total: typeof raw.total === 'number' ? raw.total : raw.items.length,
    };
  }

  const places = Array.isArray(raw.places) ? raw.places : [];
  const events = Array.isArray(raw.events) ? raw.events : [];
  const merged = [...places, ...events];

  return {
    items: merged,
    total: typeof raw.total === 'number' ? raw.total : merged.length,
  };
}

function buildParams(request: MapSearchRequest): Record<string, string | number | boolean> {
  const { filter } = request;

  return {
    lat: request.lat,
    lng: request.lng,
    radiusKm: request.radiusKm,
    types: filter.entityType,
    categories: filter.quickFilters.join(','),
    minBudget: filter.minBudget ?? '',
    maxBudget: filter.maxBudget ?? '',
    duration: filter.duration ?? '',
    date: filter.date ?? '',
    time: filter.time ?? '',
    openNow: filter.openNow,
    suitableFor: filter.suitableFor.join(','),
    sortBy: filter.sortBy,
  };
}

function isMapSearchNotImplemented(error: unknown): boolean {
  if (!axios.isAxiosError(error)) {
    return false;
  }

  const message = error.response?.data?.message;
  return (
    error.response?.status === 404 &&
    typeof message === 'string' &&
    message.includes('No static resource api/map/search')
  );
}

export const mapDiscoveryApi = {
  async search(request: MapSearchRequest): Promise<MapSearchResponse> {
    try {
      const { data } = await http.get<RawMapSearchResponse>('/api/map/search', {
        params: buildParams(request),
      });
      return normalizeResponse(data);
    } catch (error) {
      if (isMapSearchNotImplemented(error)) {
        return { items: [], total: 0 };
      }

      if (env.enableMapMockFallback) {
        return searchMapEntitiesMock(request);
      }

      throw error;
    }
  },
};
