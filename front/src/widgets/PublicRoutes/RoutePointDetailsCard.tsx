import placeholderImage from '@/assets/placeholder.png';
import type { PublicRoutePoint } from '@/entities/publicRoute/model';
import { Button } from '@/shared/ui/Button/Button';
import { useImageWithPlaceholder } from './useImageWithPlaceholder';
import styles from './RoutePointDetailsCard.module.css';

interface RoutePointDetailsCardProps {
  point: PublicRoutePoint | null;
}

function pointTypeLabel(type: PublicRoutePoint['type']): string {
  if (type === 'event') return 'Событие';
  if (type === 'place') return 'Место';
  return 'Точка маршрута';
}

export function RoutePointDetailsCard({ point }: RoutePointDetailsCardProps) {
  const imageSrc = useImageWithPlaceholder(point?.imageUrl, placeholderImage);

  if (!point) {
    return null;
  }

  return (
    <section className={styles.card}>
      <div className={styles.imageWrap}>
        <img className={styles.image} src={imageSrc} alt={point.title} loading="lazy" />
      </div>

      <div className={styles.content}>
        <div className={styles.top}>
          <span className={styles.order}>Пункт {point.order}</span>
          <span className={styles.type}>{pointTypeLabel(point.type)}</span>
          {typeof point.estimatedStopMinutes === 'number' ? (
            <span className={styles.duration}>~{point.estimatedStopMinutes} мин</span>
          ) : null}
        </div>

        <h3>{point.title}</h3>
        <p className={styles.description}>{point.description}</p>
        {point.location ? <p className={styles.location}>{point.location}</p> : null}

        <div className={styles.actions}>
          <Button>Просмотр точки</Button>
          <Button variant="ghost">Добавить в избранное</Button>
        </div>
      </div>
    </section>
  );
}
