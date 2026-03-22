import { useEffect, useRef, useState } from 'react';
import type { PublicRoutePoint } from '@/entities/publicRoute/model';
import { loadYMaps } from '@/shared/lib/yandexMaps';
import styles from './RouteMap.module.css';

interface RouteMapProps {
  points: PublicRoutePoint[];
  selectedPointId: string | null;
  onSelectPoint: (pointId: string) => void;
}

interface RenderPoint extends PublicRoutePoint {
  renderLat: number;
  renderLng: number;
}

function spreadOverlappingPoints(sortedPoints: PublicRoutePoint[]): RenderPoint[] {
  const grouped = new Map<string, PublicRoutePoint[]>();

  sortedPoints.forEach((point) => {
    const key = `${point.lat.toFixed(6)}:${point.lng.toFixed(6)}`;
    const bucket = grouped.get(key) ?? [];
    bucket.push(point);
    grouped.set(key, bucket);
  });

  const renderPoints: RenderPoint[] = [];

  grouped.forEach((group) => {
    if (group.length === 1) {
      const single = group[0];
      renderPoints.push({ ...single, renderLat: single.lat, renderLng: single.lng });
      return;
    }

    const baseLat = group[0].lat;
    const baseLng = group[0].lng;
    // ~10-20 meters radius so overlapping markers remain distinguishable.
    const radius = 0.00018;

    group.forEach((point, index) => {
      const angle = (2 * Math.PI * index) / group.length;
      renderPoints.push({
        ...point,
        renderLat: baseLat + Math.sin(angle) * radius,
        renderLng: baseLng + Math.cos(angle) * radius,
      });
    });
  });

  return renderPoints.sort((a, b) => a.order - b.order);
}

export function RouteMap({ points, selectedPointId, onSelectPoint }: RouteMapProps) {
  const containerRef = useRef<HTMLDivElement | null>(null);
  const mapRef = useRef<any>(null);
  const collectionRef = useRef<any>(null);
  const polylineRef = useRef<any>(null);
  const [error, setError] = useState<string | null>(null);
  const [isReady, setIsReady] = useState(false);
  const lastPointsSignatureRef = useRef<string>('');

  useEffect(() => {
    let mounted = true;

    void loadYMaps()
      .then((ymaps) => {
        if (!mounted || !containerRef.current) {
          return;
        }

        mapRef.current = new ymaps.Map(containerRef.current, {
          center: [45.03547, 38.97531],
          zoom: 12,
          controls: ['zoomControl'],
        });

        collectionRef.current = new ymaps.GeoObjectCollection();
        mapRef.current.geoObjects.add(collectionRef.current);
        setIsReady(true);
      })
      .catch((err: Error) => {
        if (mounted) {
          setError(err.message);
        }
      });

    return () => {
      mounted = false;
      if (mapRef.current) {
        mapRef.current.destroy();
      }
    };
  }, []);

  useEffect(() => {
    if (!isReady || !mapRef.current || !window.ymaps) {
      return;
    }

    const ymaps = window.ymaps as any;
    const collection = collectionRef.current;
    if (!collection) {
      return;
    }

    collection.removeAll();

    const sorted = [...points].sort((a, b) => a.order - b.order);
    const renderPoints = spreadOverlappingPoints(sorted);
    const pointsSignature = sorted.map((point) => `${point.id}:${point.lat}:${point.lng}`).join('|');

    // removeAll() already detaches previous line and marks from collection.
    polylineRef.current = null;

    if (sorted.length > 1) {
      polylineRef.current = new ymaps.Polyline(
        sorted.map((point) => [point.lat, point.lng]),
        {},
        {
          strokeColor: '#0ea5e9',
          strokeWidth: 4,
          strokeOpacity: 0.85,
        },
      );
      collection.add(polylineRef.current);
    }

    const marks = renderPoints.map((point) => {
      const selected = point.id === selectedPointId;
      const mark = new ymaps.Placemark(
        [point.renderLat, point.renderLng],
        {
          iconContent: String(point.order),
          hintContent: point.title,
          balloonContentHeader: point.title,
          balloonContentBody: point.description,
        },
        {
          preset: selected ? 'islands#redStretchyIcon' : 'islands#blueStretchyIcon',
        },
      );

      mark.events.add('click', () => onSelectPoint(point.id));
      return mark;
    });

    marks.forEach((mark) => collection.add(mark));

    if (renderPoints.length && lastPointsSignatureRef.current !== pointsSignature) {
      const bounds = renderPoints.map((point) => [point.renderLat, point.renderLng]);
      mapRef.current.setBounds(bounds, { checkZoomRange: true, zoomMargin: 26 });
      lastPointsSignatureRef.current = pointsSignature;
    }
  }, [isReady, onSelectPoint, points, selectedPointId]);

  return (
    <div className={styles.root}>
      {!isReady ? <div className={styles.loading}>Загружаем карту маршрута...</div> : null}
      <div className={styles.map} ref={containerRef} />
      {error ? <p className={styles.error}>{error}</p> : null}
    </div>
  );
}
