import { useEffect, useRef, useState } from 'react';
import { env } from '@/shared/config/env';
import type { MapEntity, MapPointSelection } from '@/entities/map/model';
import styles from './DiscoveryMap.module.css';

interface DiscoveryMapProps {
  items: MapEntity[];
  selection: MapPointSelection | null;
  selectedId: string | null;
  hoveredId: string | null;
  focusItem: MapEntity | null;
  onMapClick: (lat: number, lng: number) => void;
  onMarkerClick: (id: string) => void;
}

let ymapsLoader: Promise<Window['ymaps']> | null = null;

function buildYMapsUrl(): string {
  const base = 'https://api-maps.yandex.ru/2.1/?lang=ru_RU';
  if (!env.yandexMapsApiKey) {
    return base;
  }
  return `${base}&apikey=${encodeURIComponent(env.yandexMapsApiKey)}`;
}

function loadYMaps(): Promise<Window['ymaps']> {
  if (typeof window === 'undefined') {
    return Promise.reject(new Error('Yandex Maps доступны только в браузере.'));
  }

  if (window.ymaps) {
    return new Promise((resolve) => {
      window.ymaps?.ready(() => resolve(window.ymaps));
    });
  }

  if (ymapsLoader) {
    return ymapsLoader;
  }

  ymapsLoader = new Promise((resolve, reject) => {
    const script = document.createElement('script');
    script.src = buildYMapsUrl();
    script.async = true;
    script.onload = () => {
      if (!window.ymaps) {
        reject(new Error('Скрипт карты загружен, но ymaps не инициализировался.'));
        return;
      }
      window.ymaps.ready(() => resolve(window.ymaps));
    };
    script.onerror = () => reject(new Error('Не удалось загрузить скрипт Yandex Maps.'));
    document.head.appendChild(script);
  });

  return ymapsLoader;
}

function markerPreset(item: MapEntity, selectedId: string | null, hoveredId: string | null): string {
  if (item.id === selectedId) {
    return 'islands#redIcon';
  }

  if (item.id === hoveredId) {
    return 'islands#orangeIcon';
  }

  return item.type === 'event' ? 'islands#violetIcon' : 'islands#greenIcon';
}

function withOffsetForSameCoordinates(items: MapEntity[]): Array<MapEntity & { __lat: number; __lng: number }> {
  const counters = new Map<string, number>();

  return items.map((item) => {
    const key = `${item.lat.toFixed(6)}:${item.lng.toFixed(6)}`;
    const index = counters.get(key) ?? 0;
    counters.set(key, index + 1);

    if (index === 0) {
      return { ...item, __lat: item.lat, __lng: item.lng };
    }

    // Tiny deterministic shift for overlapping points to keep both selectable.
    const angle = (index * Math.PI) / 4;
    const radius = 0.00012 * Math.ceil(index / 2);

    return {
      ...item,
      __lat: item.lat + Math.sin(angle) * radius,
      __lng: item.lng + Math.cos(angle) * radius,
    };
  });
}

export function DiscoveryMap({
  items,
  selection,
  selectedId,
  hoveredId,
  focusItem,
  onMapClick,
  onMarkerClick,
}: DiscoveryMapProps) {
  const containerRef = useRef<HTMLDivElement | null>(null);
  const mapRef = useRef<any>(null);
  const clustererRef = useRef<any>(null);
  const circleRef = useRef<any>(null);
  const centerPlacemarkRef = useRef<any>(null);
  const clickEventRef = useRef<any>(null);
  const [isReady, setIsReady] = useState(false);
  const [mapError, setMapError] = useState<string | null>(null);

  const onMapClickRef = useRef(onMapClick);
  const onMarkerClickRef = useRef(onMarkerClick);

  useEffect(() => {
    onMapClickRef.current = onMapClick;
  }, [onMapClick]);

  useEffect(() => {
    onMarkerClickRef.current = onMarkerClick;
  }, [onMarkerClick]);

  useEffect(() => {
    let mounted = true;

    void loadYMaps()
      .then((ymaps) => {
        if (!mounted || !containerRef.current) {
          return;
        }

        const initialCenter: [number, number] = selection
          ? [selection.lat, selection.lng]
          : [45.03547, 38.97531];

        mapRef.current = new ymaps.Map(
          containerRef.current,
          {
            center: initialCenter,
            zoom: 12,
            controls: [],
          },
          {
            suppressMapOpenBlock: true,
          },
        );

        mapRef.current.controls.add(new ymaps.control.ZoomControl({ float: 'right' }));
        mapRef.current.controls.add(new ymaps.control.GeolocationControl({ float: 'right' }));

        clickEventRef.current = (event: any) => {
          const coords = event.get('coords') as [number, number];
          onMapClickRef.current(coords[0], coords[1]);
        };

        mapRef.current.events.add('click', clickEventRef.current);
        setIsReady(true);
      })
      .catch((error: Error) => {
        if (mounted) {
          const keyHint = env.yandexMapsApiKey
            ? ''
            : ' Укажите VITE_YANDEX_MAPS_API_KEY в .env.local и перезапустите dev-сервер.';
          setMapError(`${error.message}${keyHint}`);
        }
      });

    return () => {
      mounted = false;
      if (mapRef.current && clickEventRef.current) {
        mapRef.current.events.remove('click', clickEventRef.current);
      }
      if (mapRef.current) {
        mapRef.current.destroy();
        mapRef.current = null;
      }
    };
  }, []);

  useEffect(() => {
    if (!isReady || !mapRef.current || !window.ymaps) {
      return;
    }

    const ymaps = window.ymaps;

    if (clustererRef.current) {
      mapRef.current.geoObjects.remove(clustererRef.current);
      clustererRef.current.removeAll();
      clustererRef.current = null;
    }

    const clusterer = new ymaps.Clusterer({
      preset: 'islands#invertedBlueClusterIcons',
      groupByCoordinates: false,
      clusterOpenBalloonOnClick: false,
      clusterDisableClickZoom: false,
    });

    const displayItems = withOffsetForSameCoordinates(items);

    const marks = displayItems.map((item) => {
      const mark = new ymaps.Placemark(
        [item.__lat, item.__lng],
        {
          hintContent: item.title,
          balloonContentHeader: item.title,
          balloonContentBody: item.description,
        },
        {
          preset: markerPreset(item, selectedId, hoveredId),
        },
      );

      mark.events.add('click', () => onMarkerClickRef.current(item.id));
      mark.events.add('balloonopen', () => onMarkerClickRef.current(item.id));
      return mark;
    });

    clusterer.add(marks);
    mapRef.current.geoObjects.add(clusterer);
    clustererRef.current = clusterer;
  }, [hoveredId, isReady, items, selectedId]);

  useEffect(() => {
    if (!isReady || !mapRef.current || !window.ymaps) {
      return;
    }

    const ymaps = window.ymaps;

    if (circleRef.current) {
      mapRef.current.geoObjects.remove(circleRef.current);
      circleRef.current = null;
    }

    if (centerPlacemarkRef.current) {
      mapRef.current.geoObjects.remove(centerPlacemarkRef.current);
      centerPlacemarkRef.current = null;
    }

    if (!selection) {
      return;
    }

    centerPlacemarkRef.current = new ymaps.Placemark(
      [selection.lat, selection.lng],
      {
        hintContent: 'Центр поиска',
      },
      {
        preset: 'islands#blackCircleDotIcon',
      },
    );

    circleRef.current = new ymaps.Circle(
      [[selection.lat, selection.lng], selection.radiusKm * 1000],
      {},
      {
        fillColor: 'rgba(14, 165, 233, 0.12)',
        strokeColor: '#0ea5e9',
        strokeOpacity: 0.85,
        strokeWidth: 2,
      },
    );

    mapRef.current.geoObjects.add(centerPlacemarkRef.current);
    mapRef.current.geoObjects.add(circleRef.current);
  }, [isReady, selection]);

  useEffect(() => {
    if (!focusItem || !isReady || !mapRef.current) {
      return;
    }

    mapRef.current.setCenter([focusItem.lat, focusItem.lng], 14, { duration: 220 });
  }, [focusItem, isReady]);

  return (
    <section className={styles.root}>
      {!isReady ? <div className={styles.loading}>Загружаем карту...</div> : null}
      <div className={styles.map} ref={containerRef} />
      {mapError ? <p className={styles.error}>{mapError}</p> : null}
      <div className={styles.legend}>
        <span>
          <i className={styles.place} /> Места
        </span>
        <span>
          <i className={styles.event} /> События
        </span>
      </div>
    </section>
  );
}
