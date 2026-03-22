import { formatMinutes } from '@/shared/lib/format';
import type { PublicRouteDetails } from '@/entities/publicRoute/model';
import { Button } from '@/shared/ui/Button/Button';
import { RoutePdfExportButton } from '@/features/routes/pdf/ui/RoutePdfExportButton';
import styles from './RouteInfoPanel.module.css';

interface RouteInfoPanelProps {
  route: PublicRouteDetails;
}

export function RouteInfoPanel({ route }: RouteInfoPanelProps) {
  return (
    <section className={styles.root}>
      <h2>{route.title}</h2>
      <p>{route.description}</p>

      <div className={styles.metrics}>
        <span>{formatMinutes(route.durationMinutes)}</span>
        <span>{route.points.length} точек</span>
        <span>{typeof route.distanceKm === 'number' ? `${route.distanceKm.toFixed(1)} км` : '— км'}</span>
        <span>
          {typeof route.estimatedBudget === 'number'
            ? `${Math.round(route.estimatedBudget).toLocaleString('ru-RU')} ₽`
            : 'Бюджет: —'}
        </span>
      </div>

      <div className={styles.tags}>
        {(route.suitableFor.length ? route.suitableFor : ['solo', 'family']).slice(0, 3).map((item) => (
          <span key={item}>{item}</span>
        ))}
      </div>

      <div className={styles.actions}>
        <Button>Начать маршрут</Button>
        <RoutePdfExportButton routeId={route.id} />
      </div>
    </section>
  );
}
