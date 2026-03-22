import { useNavigate } from 'react-router-dom';
import type { HomeRouteCard } from '@/entities/home/model';
import { appRoutes } from '@/shared/const/routes';
import { Button } from '@/shared/ui/Button/Button';
import { SectionTitle } from '@/shared/ui/SectionTitle/SectionTitle';
import { RouteCard } from '@/widgets/RouteCard/RouteCard';
import { EmptyState } from '@/shared/ui/EmptyState/EmptyState';
import { ErrorState } from '@/shared/ui/ErrorState/ErrorState';
import styles from './RecommendationsSection.module.css';

interface RecommendationsSectionProps {
  isAuthenticated: boolean;
  recommendations: HomeRouteCard[];
  recommendationError: boolean;
}

export function RecommendationsSection({
  isAuthenticated,
  recommendations,
  recommendationError,
}: RecommendationsSectionProps) {
  const navigate = useNavigate();

  if (!isAuthenticated) {
    return (
      <section className={styles.guestCta}>
        <div>
          <p className={styles.kicker}>Персонализация</p>
          <h2>Войдите и получите рекомендации маршрутов именно под ваши интересы</h2>
          <p>
            После авторизации мы подберем варианты поездки по предпочтениям, темпу и формату отдыха.
          </p>
        </div>
        <Button onClick={() => navigate(appRoutes.login)}>Войти и получить рекомендации</Button>
      </section>
    );
  }

  if (recommendationError) {
    return (
      <ErrorState
        title="Рекомендации временно недоступны"
        message="Основные разделы главной работают корректно. Повторите попытку чуть позже."
      />
    );
  }

  return (
    <section className={styles.section}>
      <SectionTitle title="Рекомендации для вас" subtitle="Персональные идеи поездки на основе вашего профиля" />
      {!recommendations.length ? (
        <EmptyState
          title="Пока нет персональных рекомендаций"
          description="Заполните профиль и попробуйте выбрать другой формат поездки."
        />
      ) : (
        <div className={styles.grid}>
          {recommendations.map((route) => (
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
            />
          ))}
        </div>
      )}
    </section>
  );
}
