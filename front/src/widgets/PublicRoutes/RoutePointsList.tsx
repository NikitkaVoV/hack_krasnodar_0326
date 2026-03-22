import { useEffect, useRef } from 'react';
import type { PublicRoutePoint } from '@/entities/publicRoute/model';
import { RoutePointItem } from './RoutePointItem';
import styles from './RoutePointsList.module.css';

interface RoutePointsListProps {
  points: PublicRoutePoint[];
  selectedPointId: string | null;
  onSelectPoint: (pointId: string) => void;
}

export function RoutePointsList({ points, selectedPointId, onSelectPoint }: RoutePointsListProps) {
  const refs = useRef<Record<string, HTMLDivElement | null>>({});

  useEffect(() => {
    if (!selectedPointId) {
      return;
    }
    refs.current[selectedPointId]?.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
  }, [selectedPointId]);

  return (
    <section className={styles.wrapper}>
      <h3 className={styles.title}>Точки маршрута</h3>
      <div className={styles.list}>
        {points.map((point) => (
          <div
            key={point.id}
            ref={(element) => {
              refs.current[point.id] = element;
            }}
          >
            <RoutePointItem
              point={point}
              isSelected={point.id === selectedPointId}
              onSelect={() => onSelectPoint(point.id)}
            />
          </div>
        ))}
      </div>
    </section>
  );
}
