import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { PageContainer } from '@/shared/ui/PageContainer/PageContainer';
import { Spinner } from '@/shared/ui/Spinner/Spinner';
import { ErrorState } from '@/shared/ui/ErrorState/ErrorState';
import { EmptyState } from '@/shared/ui/EmptyState/EmptyState';
import { Button } from '@/shared/ui/Button/Button';
import { getApiErrorMessage } from '@/shared/lib/errors';
import { appRoutes } from '@/shared/const/routes';
import { useDeleteMyRouteMutation, useMyRoutesHistoryQuery } from '@/shared/hooks/useMyRoutesHistory';
import { usePublicRouteDetailsQuery } from '@/shared/hooks/usePublicRoutes';
import { PublicRouteCard } from '@/widgets/PublicRoutes/PublicRouteCard';
import { RouteDetailsModal } from '@/widgets/PublicRoutes/RouteDetailsModal';
import styles from './RouteBuilderPage.module.css';

export function RouteBuilderPage() {
  const navigate = useNavigate();
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [selectedRouteId, setSelectedRouteId] = useState<string | null>(null);

  const myRoutesQuery = useMyRoutesHistoryQuery();
  const deleteRouteMutation = useDeleteMyRouteMutation();
  const routeDetailsQuery = usePublicRouteDetailsQuery(selectedRouteId);

  const deletingRouteId = useMemo(
    () => (deleteRouteMutation.isPending ? deleteRouteMutation.variables : null),
    [deleteRouteMutation.isPending, deleteRouteMutation.variables],
  );

  const handleDelete = (routeId: string) => {
    const confirmed = window.confirm('Удалить маршрут из вашей истории?');
    if (!confirmed) {
      return;
    }

    setDeleteError(null);
    deleteRouteMutation.mutate(routeId, {
      onSuccess: () => {
        if (selectedRouteId === routeId) {
          setSelectedRouteId(null);
        }
      },
      onError: (error) => {
        setDeleteError(getApiErrorMessage(error, 'Не удалось удалить маршрут.'));
      },
    });
  };

  return (
    <PageContainer className={styles.page}>
      <section className={styles.hero}>
        <div>
          <h1>Конструктор маршрутов</h1>
          <p>Здесь хранятся ваши сохраненные маршруты. Откройте любой из них или начните собирать новый.</p>
        </div>
        <Button onClick={() => navigate(appRoutes.routeBuilderCreate)}>Построить маршрут</Button>
      </section>

      {myRoutesQuery.isLoading ? <Spinner label="Загружаем ваши маршруты..." /> : null}

      {myRoutesQuery.isError ? (
        <div className={styles.stateBlock}>
          <ErrorState
            title="Не удалось загрузить маршруты"
            message={getApiErrorMessage(myRoutesQuery.error, 'Попробуйте повторить позже.')}
          />
          <Button onClick={() => void myRoutesQuery.refetch()}>Повторить</Button>
        </div>
      ) : null}

      {deleteError ? <ErrorState title="Ошибка удаления" message={deleteError} /> : null}

      {!myRoutesQuery.isLoading && !myRoutesQuery.isError && !(myRoutesQuery.data ?? []).length ? (
        <div className={styles.stateBlock}>
          <EmptyState
            title="У вас пока нет маршрутов"
            description="Сохраненные маршруты появятся здесь. Начните с создания первого маршрута."
          />
          <div className={styles.actions}>
            <Button onClick={() => navigate(appRoutes.routeBuilderCreate)}>Создать маршрут</Button>
            <Button variant="ghost" onClick={() => navigate(appRoutes.routes)}>
              Перейти к готовым маршрутам
            </Button>
          </div>
        </div>
      ) : null}

      {(myRoutesQuery.data ?? []).length ? (
        <section className={styles.grid}>
          {(myRoutesQuery.data ?? []).map((route) => (
            <PublicRouteCard
              key={route.id}
              route={route}
              onOpen={() => setSelectedRouteId(route.id)}
              onDelete={() => handleDelete(route.id)}
              isDeleting={deletingRouteId === route.id}
              openLabel="Открыть"
            />
          ))}
        </section>
      ) : null}

      <RouteDetailsModal
        isOpen={Boolean(selectedRouteId)}
        route={routeDetailsQuery.data ?? null}
        isLoading={routeDetailsQuery.isLoading}
        onClose={() => setSelectedRouteId(null)}
      />
    </PageContainer>
  );
}
