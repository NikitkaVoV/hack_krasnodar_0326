import { useState } from 'react';
import type { HomeRouteCard } from '@/entities/home/model';
import { usePublicRouteDetailsQuery } from '@/shared/hooks/usePublicRoutes';
import { SectionTitle } from '@/shared/ui/SectionTitle/SectionTitle';
import { RouteCard } from '@/widgets/RouteCard/RouteCard';
import { RouteDetailsModal } from '@/widgets/PublicRoutes/RouteDetailsModal';
import { EmptyState } from '@/shared/ui/EmptyState/EmptyState';
import styles from './PopularRoutesSection.module.css';

interface PopularRoutesSectionProps {
  routes: HomeRouteCard[];
}

export function PopularRoutesSection({ routes }: PopularRoutesSectionProps) {
  const [selectedRouteId, setSelectedRouteId] = useState<string | null>(null);
  const detailsQuery = usePublicRouteDetailsQuery(selectedRouteId);

  return (
    <section className={styles.section}>
      <SectionTitle title="Популярные маршруты" subtitle="Готовые решения с датой, длительностью и быстрым стартом" />
      {!routes.length ? (
        <EmptyState
          title="Популярные маршруты временно недоступны"
          description="Мы обновляем витрину. Попробуйте обновить страницу немного позже."
        />
      ) : (
        <div className={styles.grid}>
          {routes.map((route) => (
            <RouteCard
              key={route.id}
              route={{
                id: route.id,
                summary: route.summary,
                date: route.date,
                totalDurationMinutes: route.totalDurationMinutes,
                advice: route.advice,
                userId: '',
              }}
              stepsCount={route.stepsCount}
              onOpen={() => setSelectedRouteId(route.id)}
            />
          ))}
        </div>
      )}

      <RouteDetailsModal
        isOpen={Boolean(selectedRouteId)}
        route={detailsQuery.data ?? null}
        isLoading={detailsQuery.isLoading}
        onClose={() => setSelectedRouteId(null)}
      />
    </section>
  );
}
