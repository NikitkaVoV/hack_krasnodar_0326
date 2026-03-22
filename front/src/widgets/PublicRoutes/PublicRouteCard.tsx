import { formatMinutes } from '@/shared/lib/format';
import type { PublicRouteCard } from '@/entities/publicRoute/model';
import placeholderImage from '@/assets/placeholder.png';
import { Button } from '@/shared/ui/Button/Button';
import { RoutePdfExportButton } from '@/features/routes/pdf/ui/RoutePdfExportButton';
import styles from './PublicRouteCard.module.css';

interface PublicRouteCardProps {
  route: PublicRouteCard;
  onOpen: () => void;
  onDelete?: () => void;
  isDeleting?: boolean;
  openLabel?: string;
}

export function PublicRouteCard({
  route,
  onOpen,
  onDelete,
  isDeleting = false,
  openLabel = 'Открыть',
}: PublicRouteCardProps) {
  const image = route.imageUrl || placeholderImage;

  return (
    <article className={styles.card} onClick={onOpen}>
      <div className={styles.imageWrap}>
        <img className={styles.image} src={image} alt={route.title} loading="lazy" />
      </div>
      <div className={styles.content}>
        <div className={styles.head}>
          <h3>{route.title}</h3>
          <p>{route.description}</p>
        </div>

        <div className={styles.metrics}>
          <span>{formatMinutes(route.durationMinutes)}</span>
          <span>{route.pointsCount} точек</span>
          <span>{typeof route.distanceKm === 'number' ? `${route.distanceKm.toFixed(1)} км` : '— км'}</span>
          <span>
            {typeof route.estimatedBudget === 'number'
              ? `${Math.round(route.estimatedBudget).toLocaleString('ru-RU')} ₽`
              : 'Бюджет: —'}
          </span>
        </div>

        <div className={styles.badges}>
          {route.badges.slice(0, 3).map((badge) => (
            <span key={badge}>{badge}</span>
          ))}
          {route.tags.slice(0, 2).map((tag) => (
            <span key={tag} className={styles.tag}>
              {tag}
            </span>
          ))}
        </div>

        <div className={styles.actions}>
          <Button
            onClick={(event) => {
              event.stopPropagation();
              onOpen();
            }}
            fullWidth
          >
            {openLabel}
          </Button>

          <RoutePdfExportButton routeId={route.id} fullWidth compact />

          {onDelete ? (
            <Button
              variant="danger"
              onClick={(event) => {
                event.stopPropagation();
                onDelete();
              }}
              disabled={isDeleting}
              fullWidth
            >
              {isDeleting ? 'Удаляем...' : 'Удалить'}
            </Button>
          ) : null}
        </div>
      </div>
    </article>
  );
}
