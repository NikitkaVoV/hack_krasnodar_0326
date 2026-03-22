import { useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { PageContainer } from '@/shared/ui/PageContainer/PageContainer';
import { getApiErrorMessage } from '@/shared/lib/errors';
import type { PublicRouteCard, PublicRoutesFilterState } from '@/entities/publicRoute/model';
import { usePublicRouteDetailsQuery, usePublicRoutesCatalogQuery } from '@/shared/hooks/usePublicRoutes';
import { RoutesHeader } from '@/widgets/PublicRoutes/RoutesHeader';
import { RoutesFilters } from '@/widgets/PublicRoutes/RoutesFilters';
import { PublicRouteCard as RouteCard } from '@/widgets/PublicRoutes/PublicRouteCard';
import {
  PublicRoutesEmptyState,
  PublicRoutesErrorState,
  PublicRoutesLoadingState,
} from '@/widgets/PublicRoutes/PublicRoutesStates';
import { RouteDetailsModal } from '@/widgets/PublicRoutes/RouteDetailsModal';
import styles from './RoutesPage.module.css';

const QUICK_TAGS = new Set(['weekend', 'family', 'gastro', 'nature', 'culture']);

const DEFAULT_FILTERS: PublicRoutesFilterState = {
  searchQuery: '',
  date: new Date().toISOString().slice(0, 10),
  quickTags: [],
  duration: 'any',
  budget: 'any',
  category: 'all',
  suitableFor: 'all',
  sortBy: 'popular',
};

function parseDuration(value: string | null): PublicRoutesFilterState['duration'] {
  if (value === 'short' || value === 'medium' || value === 'long') {
    return value;
  }
  return 'any';
}

function parseBudget(value: string | null): PublicRoutesFilterState['budget'] {
  if (value === 'low' || value === 'medium' || value === 'high') {
    return value;
  }
  return 'any';
}

function parseCategory(value: string | null): PublicRoutesFilterState['category'] {
  if (value === 'city' || value === 'nature' || value === 'gastro' || value === 'culture') {
    return value;
  }
  return 'all';
}

function parseSuitableFor(value: string | null): PublicRoutesFilterState['suitableFor'] {
  if (value === 'solo' || value === 'family' || value === 'groups') {
    return value;
  }
  return 'all';
}

function parseSortBy(value: string | null): PublicRoutesFilterState['sortBy'] {
  if (value === 'duration' || value === 'distance' || value === 'budget') {
    return value;
  }
  return 'popular';
}

function parseQuickTags(value: string | null): string[] {
  if (!value) {
    return [];
  }

  return value
    .split(',')
    .map((item) => item.trim())
    .filter((item) => QUICK_TAGS.has(item));
}

function parseDate(value: string | null): string {
  if (value && /^\d{4}-\d{2}-\d{2}$/.test(value)) {
    return value;
  }
  return DEFAULT_FILTERS.date;
}

function buildFiltersFromSearchParams(searchParams: URLSearchParams): PublicRoutesFilterState {
  return {
    searchQuery: searchParams.get('q')?.trim() ?? '',
    date: parseDate(searchParams.get('date')),
    quickTags: parseQuickTags(searchParams.get('tags')),
    duration: parseDuration(searchParams.get('duration')),
    budget: parseBudget(searchParams.get('budget')),
    category: parseCategory(searchParams.get('category')),
    suitableFor: parseSuitableFor(searchParams.get('suitableFor')),
    sortBy: parseSortBy(searchParams.get('sortBy')),
  };
}

function matchesSearch(route: PublicRouteCard, searchQuery: string): boolean {
  if (!searchQuery.trim()) {
    return true;
  }

  const normalizedQuery = searchQuery.toLowerCase();
  return (
    route.title.toLowerCase().includes(normalizedQuery) ||
    route.description.toLowerCase().includes(normalizedQuery) ||
    route.tags.some((tag) => tag.toLowerCase().includes(normalizedQuery)) ||
    route.badges.some((badge) => badge.toLowerCase().includes(normalizedQuery))
  );
}

function matchesDuration(route: PublicRouteCard, duration: PublicRoutesFilterState['duration']): boolean {
  if (duration === 'any') return true;
  if (duration === 'short') return route.durationMinutes <= 120;
  if (duration === 'medium') return route.durationMinutes > 120 && route.durationMinutes <= 300;
  return route.durationMinutes > 300;
}

function matchesBudget(route: PublicRouteCard, budget: PublicRoutesFilterState['budget']): boolean {
  if (budget === 'any' || typeof route.estimatedBudget !== 'number') return true;
  if (budget === 'low') return route.estimatedBudget <= 1500;
  if (budget === 'medium') return route.estimatedBudget > 1500 && route.estimatedBudget <= 4000;
  return route.estimatedBudget > 4000;
}

function matchesCategory(route: PublicRouteCard, category: PublicRoutesFilterState['category']): boolean {
  if (category === 'all') return true;
  return route.category === category || route.tags.includes(category);
}

function matchesSuitableFor(route: PublicRouteCard, suitableFor: PublicRoutesFilterState['suitableFor']): boolean {
  if (suitableFor === 'all') return true;
  return route.suitableFor.includes(suitableFor) || route.tags.includes(suitableFor);
}

function matchesQuickTags(route: PublicRouteCard, quickTags: string[]): boolean {
  if (!quickTags.length) return true;
  return quickTags.every((tag) => route.tags.includes(tag) || route.badges.includes(tag as PublicRouteCard['badges'][number]));
}

function sortRoutes(routes: PublicRouteCard[], sortBy: PublicRoutesFilterState['sortBy']): PublicRouteCard[] {
  const sorted = [...routes];

  if (sortBy === 'duration') {
    sorted.sort((a, b) => a.durationMinutes - b.durationMinutes);
    return sorted;
  }

  if (sortBy === 'distance') {
    sorted.sort((a, b) => {
      const av = typeof a.distanceKm === 'number' ? a.distanceKm : Number.MAX_SAFE_INTEGER;
      const bv = typeof b.distanceKm === 'number' ? b.distanceKm : Number.MAX_SAFE_INTEGER;
      return av - bv;
    });
    return sorted;
  }

  if (sortBy === 'budget') {
    sorted.sort((a, b) => {
      const av = typeof a.estimatedBudget === 'number' ? a.estimatedBudget : Number.MAX_SAFE_INTEGER;
      const bv = typeof b.estimatedBudget === 'number' ? b.estimatedBudget : Number.MAX_SAFE_INTEGER;
      return av - bv;
    });
    return sorted;
  }

  sorted.sort((a, b) => b.badges.length - a.badges.length);
  return sorted;
}

export function RoutesPage() {
  const [searchParams] = useSearchParams();
  const searchParamsKey = searchParams.toString();
  const searchParamsFilters = useMemo(
    () => buildFiltersFromSearchParams(new URLSearchParams(searchParamsKey)),
    [searchParamsKey],
  );

  const [filters, setFilters] = useState<PublicRoutesFilterState>(searchParamsFilters);
  const [selectedRouteId, setSelectedRouteId] = useState<string | null>(null);

  useEffect(() => {
    setFilters(searchParamsFilters);
  }, [searchParamsFilters]);

  const catalogQuery = usePublicRoutesCatalogQuery(24, filters.date);
  const detailsQuery = usePublicRouteDetailsQuery(selectedRouteId);

  const filteredRoutes = useMemo(() => {
    const source = catalogQuery.data ?? [];

    const filtered = source
      .filter((route) => matchesSearch(route, filters.searchQuery))
      .filter((route) => matchesDuration(route, filters.duration))
      .filter((route) => matchesBudget(route, filters.budget))
      .filter((route) => matchesCategory(route, filters.category))
      .filter((route) => matchesSuitableFor(route, filters.suitableFor))
      .filter((route) => matchesQuickTags(route, filters.quickTags));

    return sortRoutes(filtered, filters.sortBy);
  }, [catalogQuery.data, filters]);

  return (
    <PageContainer className={styles.page}>
      <RoutesHeader total={catalogQuery.data?.length ?? 0} />
      <RoutesFilters value={filters} onChange={setFilters} />

      {catalogQuery.isLoading ? <PublicRoutesLoadingState /> : null}
      {catalogQuery.isError ? (
        <PublicRoutesErrorState
          message={getApiErrorMessage(catalogQuery.error, 'Не удалось загрузить публичные маршруты.')}
          onRetry={() => void catalogQuery.refetch()}
        />
      ) : null}

      {!catalogQuery.isLoading && !catalogQuery.isError && !filteredRoutes.length ? (
        <PublicRoutesEmptyState />
      ) : null}

      {!!filteredRoutes.length ? (
        <section className={styles.grid}>
          {filteredRoutes.map((route) => (
            <RouteCard key={route.id} route={route} onOpen={() => setSelectedRouteId(route.id)} />
          ))}
        </section>
      ) : null}

      <RouteDetailsModal
        isOpen={Boolean(selectedRouteId)}
        route={detailsQuery.data ?? null}
        isLoading={detailsQuery.isLoading}
        onClose={() => setSelectedRouteId(null)}
      />
    </PageContainer>
  );
}
