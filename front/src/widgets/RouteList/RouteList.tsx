import type { AppRoute } from '@/entities/route/model';
import { RouteCard } from '@/widgets/RouteCard/RouteCard';
import styles from './RouteList.module.css';

interface RouteListProps {
  routes: AppRoute[];
}

export function RouteList({ routes }: RouteListProps) {
  return (
    <div className={styles.grid}>
      {routes.map((route) => (
        <RouteCard key={route.id} route={route} />
      ))}
    </div>
  );
}


