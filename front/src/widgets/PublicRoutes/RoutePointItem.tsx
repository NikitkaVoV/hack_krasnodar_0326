import type { PublicRoutePoint } from '@/entities/publicRoute/model';
import styles from './RoutePointItem.module.css';

interface RoutePointItemProps {
  point: PublicRoutePoint;
  isSelected: boolean;
  onSelect: () => void;
}

export function RoutePointItem({ point, isSelected, onSelect }: RoutePointItemProps) {
  return (
    <button
      type="button"
      className={`${styles.item} ${isSelected ? styles.selected : ''}`}
      onClick={onSelect}
      aria-pressed={isSelected}
    >
      <span className={styles.order}>{point.order}</span>
      <span className={styles.content}>
        <span className={styles.title}>{point.title}</span>
        <span className={styles.description}>{point.description}</span>
        <span className={styles.meta}>
          <span>{point.type === 'event' ? 'Событие' : 'Локация'}</span>
          {typeof point.estimatedStopMinutes === 'number' ? <span>{point.estimatedStopMinutes} мин</span> : null}
          {point.location ? <span>{point.location}</span> : null}
        </span>
      </span>
    </button>
  );
}
