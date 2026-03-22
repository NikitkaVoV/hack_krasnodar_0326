import { Link } from 'react-router-dom';
import { Card } from '@/shared/ui/Card/Card';
import { appRoutes } from '@/shared/const/routes';
import type { AppRoute } from '@/entities/route/model';
import { formatDate, formatMinutes } from '@/shared/lib/format';
import { Button } from '@/shared/ui/Button/Button';
import styles from './RouteCard.module.css';

interface RouteCardProps {
  route: AppRoute;
  stepsCount?: number;
  onOpen?: () => void;
}

export function RouteCard({ route, stepsCount, onOpen }: RouteCardProps) {
  return (
    <Card className={styles.card}>
      <div className={styles.visual} />
      <h3>{route.summary}</h3>
      <p>{route.advice || 'Добавим полезные рекомендации для этого маршрута в ближайшее время.'}</p>
      <div className={styles.meta}>
        <span>Дата: {formatDate(route.date)}</span>
        <span>Длительность: {formatMinutes(route.totalDurationMinutes)}</span>
        <span>Шагов: {typeof stepsCount === 'number' ? stepsCount : '—'}</span>
      </div>

      {onOpen ? (
        <Button onClick={onOpen} fullWidth>
          Открыть маршрут
        </Button>
      ) : (
        <Link to={appRoutes.routeDetails(route.id)} className={styles.link}>
          <Button fullWidth>Открыть маршрут</Button>
        </Link>
      )}
    </Card>
  );
}
