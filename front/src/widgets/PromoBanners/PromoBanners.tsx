import { useNavigate } from 'react-router-dom';
import { appRoutes } from '@/shared/const/routes';
import { Button } from '@/shared/ui/Button/Button';
import styles from './PromoBanners.module.css';

export function PromoBanners() {
  const navigate = useNavigate();

  return (
    <section className={styles.grid}>
      <article className={styles.bannerPrimary}>
        <div className={styles.overlay} />
        <div className={styles.content}>
          <h3>Соберите маршрут под свой ритм поездки</h3>
          <p>Конструктор поможет учесть интересы, длительность и желаемый формат отдыха.</p>
          <Button onClick={() => navigate(appRoutes.routeBuilder)}>Открыть конструктор</Button>
        </div>
      </article>

      <article className={styles.bannerSecondary}>
        <div className={styles.overlay} />
        <div className={styles.content}>
          <h3>Откройте новые места и события</h3>
          <p>Исследуйте актуальные подборки и популярные маршруты для вашей следующей поездки.</p>
          <Button variant="secondary" onClick={() => navigate(appRoutes.routes)}>
            Смотреть маршруты
          </Button>
        </div>
      </article>
    </section>
  );
}
