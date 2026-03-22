import type { CSSProperties } from 'react';
import { useNavigate } from 'react-router-dom';
import homeGor from '@/assets/home_gor.png';
import { appRoutes } from '@/shared/const/routes';
import { Button } from '@/shared/ui/Button/Button';
import styles from './FinalCta.module.css';

interface FinalCtaProps {
  isAuthenticated: boolean;
}

export function FinalCta({ isAuthenticated }: FinalCtaProps) {
  const navigate = useNavigate();

  return (
    <section className={styles.cta} style={{ '--cta-bg-image': `url(${homeGor})` } as CSSProperties}>
      <h2>{isAuthenticated ? 'Пора спланировать следующую поездку' : 'Начните с персональных рекомендаций'}</h2>
      <p>
        {isAuthenticated
          ? 'Соберите новый маршрут с учетом ваших интересов и актуальных событий.'
          : 'Войдите в аккаунт и получите персональную витрину маршрутов и событий.'}
      </p>
      <div className={styles.actions}>
        <Button onClick={() => navigate(isAuthenticated ? appRoutes.routeBuilder : appRoutes.login)}>
          {isAuthenticated ? 'Собрать маршрут' : 'Войти'}
        </Button>
        <Button variant="ghost" onClick={() => navigate(appRoutes.routes)}>
          Перейти к маршрутам
        </Button>
      </div>
    </section>
  );
}
