import { useNavigate, useParams } from 'react-router-dom';
import { PageContainer } from '@/shared/ui/PageContainer/PageContainer';
import { Button } from '@/shared/ui/Button/Button';
import { Card } from '@/shared/ui/Card/Card';
import { appRoutes } from '@/shared/const/routes';
import styles from './PlaceDetailsPage.module.css';

export function PlaceDetailsPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  return (
    <PageContainer className={styles.page}>
      <section className={styles.hero}>
        <h1>Локация</h1>
        <p>ID места: {id ?? 'не указан'}</p>
        <p>Страница места в разработке. Здесь будет полный профиль локации и связанные события.</p>
        <div className={styles.actions}>
          <Button variant="ghost" onClick={() => navigate(appRoutes.routes)}>
            К маршрутам
          </Button>
          <Button onClick={() => navigate(appRoutes.mapDiscovery)}>Открыть на карте</Button>
        </div>
      </section>

      <Card>
        <h3>Что будет дальше</h3>
        <p>Фото, расписание, отзывы и интеграция в публичные маршруты.</p>
      </Card>
    </PageContainer>
  );
}
