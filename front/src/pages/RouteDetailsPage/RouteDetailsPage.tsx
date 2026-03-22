import { useNavigate, useParams } from 'react-router-dom';
import { PageContainer } from '@/shared/ui/PageContainer/PageContainer';
import { Card } from '@/shared/ui/Card/Card';
import { Button } from '@/shared/ui/Button/Button';
import { appRoutes } from '@/shared/const/routes';
import styles from './RouteDetailsPage.module.css';

export function RouteDetailsPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  return (
    <PageContainer className={styles.page}>
      <section className={styles.hero}>
        <h1>Детали маршрута</h1>
        <p>Маршрут: {id ?? 'не указан'}</p>
        <p>Страница в разработке. Здесь будет подробный план поездки с шагами и таймингом.</p>
        <div className={styles.actions}>
          <Button variant="ghost" onClick={() => navigate(appRoutes.routes)}>
            К маршрутам
          </Button>
          <Button onClick={() => navigate(appRoutes.routeBuilder)}>Собрать похожий</Button>
        </div>
      </section>

      <Card className={styles.card}>
        <h3>Что появится</h3>
        <p>Карта, список точек, события по пути и практические советы по логистике.</p>
      </Card>
    </PageContainer>
  );
}
