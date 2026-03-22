import { useEffect, useMemo, useRef, useState } from 'react';
import type { RouteBuilderRecommendation, RouteBuilderStep } from '@/entities/routeBuilder/model';
import { loadYMaps } from '@/shared/lib/yandexMaps';
import styles from './BuilderMapPanel.module.css';

interface BuilderMapPanelProps {
  center: { lat: number; lng: number };
  radiusKm: number;
  steps: RouteBuilderStep[];
  recommendations: RouteBuilderRecommendation[];
  selectedStepId: string | null;
  onPickStep: (sourceId: string) => void;
  onPickRecommendation: (sourceId: string) => void;
  onMapClick: (lat: number, lng: number) => void;
}

function buildRouteKey(steps: RouteBuilderStep[]): string {
  return steps.map((step) => step.sourceId).join('|');
}

export function BuilderMapPanel({
  center,
  radiusKm,
  steps,
  recommendations,
  selectedStepId,
  onPickStep,
  onPickRecommendation,
  onMapClick,
}: BuilderMapPanelProps) {
  const containerRef = useRef<HTMLDivElement | null>(null);
  const mapRef = useRef<any>(null);
  const clickHandlerRef = useRef<any>(null);
  const onMapClickRef = useRef(onMapClick);
  const onPickStepRef = useRef(onPickStep);
  const onPickRecommendationRef = useRef(onPickRecommendation);
  const lastViewportRouteKeyRef = useRef<string>('');
  const [error, setError] = useState<string | null>(null);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    onMapClickRef.current = onMapClick;
  }, [onMapClick]);

  useEffect(() => {
    onPickStepRef.current = onPickStep;
  }, [onPickStep]);

  useEffect(() => {
    onPickRecommendationRef.current = onPickRecommendation;
  }, [onPickRecommendation]);

  useEffect(() => {
    let mounted = true;

    void loadYMaps()
      .then((ymaps) => {
        if (!mounted || !containerRef.current) {
          return;
        }

        mapRef.current = new ymaps.Map(
          containerRef.current,
          {
            center: [center.lat, center.lng],
            zoom: 12,
            controls: [],
          },
          {
            suppressMapOpenBlock: true,
          },
        );

        mapRef.current.controls.add(new ymaps.control.ZoomControl({ float: 'right' }));

        clickHandlerRef.current = (event: any) => {
          const coords = event.get('coords') as [number, number];
          onMapClickRef.current(coords[0], coords[1]);
        };

        mapRef.current.events.add('click', clickHandlerRef.current);
        setReady(true);
      })
      .catch((err: Error) => {
        if (mounted) {
          setError(err.message);
        }
      });

    return () => {
      mounted = false;
      if (mapRef.current && clickHandlerRef.current) {
        mapRef.current.events.remove('click', clickHandlerRef.current);
      }
      if (mapRef.current) {
        mapRef.current.destroy();
        mapRef.current = null;
      }
    };
  }, []);

  const flattenedRecommendations = useMemo(
    () =>
      recommendations.filter(
        (item, index, source) => source.findIndex((entry) => entry.sourceId === item.sourceId) === index,
      ),
    [recommendations],
  );

  useEffect(() => {
    if (!ready || !mapRef.current || !window.ymaps) {
      return;
    }

    const ymaps = window.ymaps;
    const map = mapRef.current;
    map.geoObjects.removeAll();

    const centerPlacemark = new ymaps.Placemark(
      [center.lat, center.lng],
      { hintContent: 'Центр подбора' },
      { preset: 'islands#blackCircleDotIcon' },
    );

    const radiusCircle = new ymaps.Circle(
      [[center.lat, center.lng], radiusKm * 1000],
      {},
      {
        fillColor: 'rgba(153, 69, 155, 0.12)',
        strokeColor: '#99459b',
        strokeOpacity: 0.85,
        strokeWidth: 2,
      },
    );

    map.geoObjects.add(radiusCircle);
    map.geoObjects.add(centerPlacemark);

    if (steps.length > 1) {
      const polyline = new ymaps.Polyline(
        steps.map((step) => [step.lat, step.lng]),
        {},
        {
          strokeColor: '#99459b',
          strokeWidth: 4,
          strokeOpacity: 0.8,
        },
      );
      map.geoObjects.add(polyline);
    }

    steps.forEach((step) => {
      const stepMark = new ymaps.Placemark(
        [step.lat, step.lng],
        {
          hintContent: `${step.order}. ${step.title}`,
          iconContent: String(step.order),
        },
        {
          preset: step.sourceId === selectedStepId ? 'islands#redStretchyIcon' : 'islands#blueStretchyIcon',
        },
      );

      stepMark.events.add('click', () => onPickStepRef.current(step.sourceId));
      map.geoObjects.add(stepMark);
    });

    flattenedRecommendations.forEach((item) => {
      const recMark = new ymaps.Placemark(
        [item.lat, item.lng],
        {
          hintContent: item.title,
        },
        {
          preset: item.type === 'event' ? 'islands#violetCircleIcon' : 'islands#greenCircleIcon',
        },
      );

      recMark.events.add('click', () => onPickRecommendationRef.current(item.sourceId));
      map.geoObjects.add(recMark);
    });
  }, [center.lat, center.lng, flattenedRecommendations, radiusKm, ready, selectedStepId, steps]);

  useEffect(() => {
    if (!ready || !mapRef.current || !window.ymaps) {
      return;
    }

    const map = mapRef.current;
    const routeKey = buildRouteKey(steps);

    if (steps.length >= 2) {
      if (lastViewportRouteKeyRef.current !== routeKey) {
        const bounds = window.ymaps.util.bounds.fromPoints(steps.map((step) => [step.lat, step.lng]));
        map.setBounds(bounds, { checkZoomRange: true, zoomMargin: [36, 36, 36, 36], duration: 180 });
        lastViewportRouteKeyRef.current = routeKey;
      }
      return;
    }

    if (steps.length === 1) {
      if (lastViewportRouteKeyRef.current !== routeKey) {
        map.setCenter([steps[0].lat, steps[0].lng], 13, { duration: 160 });
        lastViewportRouteKeyRef.current = routeKey;
      }
      return;
    }

    map.setCenter([center.lat, center.lng], 12, { duration: 120 });
    lastViewportRouteKeyRef.current = '';
  }, [center.lat, center.lng, ready, steps]);

  return (
    <section className={styles.root}>
      <header className={styles.header}>
        <h2>Карта маршрута</h2>
        <p>Клик по карте задаёт новую зону подбора. Маркеры синхронизированы со списками.</p>
      </header>

      <div className={styles.mapWrap}>
        {!ready ? <div className={styles.overlay}>Загружаем карту...</div> : null}
        {error ? <div className={styles.overlay}>{error}</div> : null}
        <div className={styles.map} ref={containerRef} />
      </div>

      <div className={styles.legend}>
        <span>
          <i className={styles.routeMark} /> Шаги маршрута
        </span>
        <span>
          <i className={styles.placeMark} /> Рекомендации мест
        </span>
        <span>
          <i className={styles.eventMark} /> Рекомендации событий
        </span>
      </div>
    </section>
  );
}
