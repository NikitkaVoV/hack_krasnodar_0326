import { useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { PageContainer } from '@/shared/ui/PageContainer/PageContainer';
import { mapDiscoveryApi } from '@/shared/api/mapDiscoveryApi';
import { useDebouncedValue } from '@/shared/hooks/useDebouncedValue';
import type { FilterState, MapEntity, MapPointSelection, MapSearchRequest } from '@/entities/map/model';
import { DiscoveryMap } from '@/widgets/MapDiscovery/DiscoveryMap';
import { RadiusControl } from '@/widgets/MapDiscovery/RadiusControl';
import { QuickFilters } from '@/widgets/MapDiscovery/QuickFilters';
import { AdvancedFilters } from '@/widgets/MapDiscovery/AdvancedFilters';
import { ResultList } from '@/widgets/MapDiscovery/ResultList';
import { ResultPreviewSheet } from '@/widgets/MapDiscovery/ResultPreviewSheet';
import {
  DiscoveryEmptyResultsState,
  DiscoveryErrorState,
  DiscoveryLoadingState,
} from '@/widgets/MapDiscovery/DiscoveryStates';
import { DiscoveryActions } from '@/widgets/MapDiscovery/DiscoveryActions';
import { Button } from '@/shared/ui/Button/Button';
import { getApiErrorMessage } from '@/shared/lib/errors';
import styles from './MapDiscoveryPage.module.css';

const KRASNODAR_CENTER: MapPointSelection = {
  lat: 45.03547,
  lng: 38.97531,
  radiusKm: 10,
};

const DEFAULT_FILTERS: FilterState = {
  quickFilters: [],
  entityType: 'both',
  openNow: false,
  suitableFor: [],
  sortBy: 'relevance',
  duration: 'any',
};

export function MapDiscoveryPage() {
  const [selection, setSelection] = useState<MapPointSelection>(KRASNODAR_CENTER);
  const [radiusKm, setRadiusKm] = useState(10);
  const [filters, setFilters] = useState<FilterState>(DEFAULT_FILTERS);
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [hoveredId, setHoveredId] = useState<string | null>(null);
  const [previewId, setPreviewId] = useState<string | null>(null);
  const [viewMode, setViewMode] = useState<'carousel' | 'list'>('carousel');
  const [activeAction, setActiveAction] = useState<string | null>(null);

  const searchRequest = useMemo<MapSearchRequest>(
    () => ({
      lat: selection.lat,
      lng: selection.lng,
      radiusKm,
      filter: filters,
    }),
    [filters, radiusKm, selection.lat, selection.lng],
  );

  const debouncedRequest = useDebouncedValue(searchRequest, 320);

  const searchQuery = useQuery({
    queryKey: ['map-discovery', debouncedRequest],
    queryFn: async () => mapDiscoveryApi.search(debouncedRequest),
  });

  const items = searchQuery.data?.items ?? [];

  const previewItem = useMemo<MapEntity | null>(
    () => items.find((item) => item.id === previewId) ?? null,
    [items, previewId],
  );

  const focusItem = useMemo<MapEntity | null>(
    () => items.find((item) => item.id === selectedId) ?? null,
    [items, selectedId],
  );

  const actionMessage = useMemo(() => {
    if (!activeAction) {
      return null;
    }

    return 'Режим включен как product-hook. Здесь можно подключить отдельную бизнес-логику.';
  }, [activeAction]);

  return (
    <PageContainer className={styles.page}>
      <section className={styles.heading}>
        <p className={styles.kicker}>Map Discovery</p>
        <h1>Открывайте места и события прямо на карте</h1>
        <p>
          Карта стартует в Краснодаре. Выберите новую точку, настройте радиус и исследуйте подборку
          мест и событий поблизости.
        </p>
      </section>

      <DiscoveryMap
        items={items}
        selection={{ ...selection, radiusKm }}
        selectedId={selectedId}
        hoveredId={hoveredId}
        focusItem={focusItem}
        onMapClick={(lat, lng) => {
          setSelection({ lat, lng, radiusKm });
        }}
        onMarkerClick={(id) => {
          setSelectedId(id);
          setPreviewId(id);
        }}
      />

      <section className={styles.controlsSticky}>
        <div className={styles.controls}>
          <RadiusControl
            valueKm={radiusKm}
            onChange={(nextRadius) => {
              setRadiusKm(nextRadius);
              setSelection((prev) => ({ ...prev, radiusKm: nextRadius }));
            }}
          />

          <div className={styles.filterGroup}>
            <QuickFilters
              selected={filters.quickFilters}
              onToggle={(key) =>
                setFilters((prev) => ({
                  ...prev,
                  quickFilters: prev.quickFilters.includes(key)
                    ? prev.quickFilters.filter((item) => item !== key)
                    : [...prev.quickFilters, key],
                }))
              }
            />
            <AdvancedFilters value={filters} onChange={setFilters} />
          </div>
        </div>

        <DiscoveryActions
          active={activeAction}
          onPick={(key) => {
            setActiveAction((prev) => (prev === key ? null : key));
          }}
        />

        {actionMessage ? <p className={styles.actionMessage}>{actionMessage}</p> : null}
      </section>

      <section className={styles.resultsHeader}>
        <div>
          <h2>Найдено: {searchQuery.data?.total ?? 0}</h2>
          <p>Карточки синхронизированы с картой. Выберите маркер или карточку для фокуса.</p>
        </div>
        <div className={styles.viewSwitch}>
          <Button variant={viewMode === 'carousel' ? 'primary' : 'ghost'} onClick={() => setViewMode('carousel')}>
            Карусель
          </Button>
          <Button variant={viewMode === 'list' ? 'primary' : 'ghost'} onClick={() => setViewMode('list')}>
            Список
          </Button>
        </div>
      </section>

      {searchQuery.isLoading ? <DiscoveryLoadingState /> : null}
      {searchQuery.isFetching && !searchQuery.isLoading ? (
        <p className={styles.refreshing}>Обновляем результаты...</p>
      ) : null}
      {searchQuery.isError ? (
        <DiscoveryErrorState message={getApiErrorMessage(searchQuery.error, 'События и места временно недоступны.')} />
      ) : null}
      {!searchQuery.isLoading && !searchQuery.isError && !items.length ? <DiscoveryEmptyResultsState /> : null}

      {items.length ? (
        <ResultList
          items={items}
          selectedId={selectedId}
          mode={viewMode}
          onSelect={(id) => {
            setSelectedId(id);
            setPreviewId(id);
          }}
          onHover={(id) => setHoveredId(id)}
        />
      ) : null}

      <ResultPreviewSheet item={previewItem} onClose={() => setPreviewId(null)} />
    </PageContainer>
  );
}
