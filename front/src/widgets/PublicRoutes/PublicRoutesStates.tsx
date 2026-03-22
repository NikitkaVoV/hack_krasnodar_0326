import { Button } from '@/shared/ui/Button/Button';
import { Card } from '@/shared/ui/Card/Card';
import styles from './PublicRoutesStates.module.css';

export function PublicRoutesLoadingState() {
  return (
    <div className={styles.grid}>
      {Array.from({ length: 6 }).map((_, index) => (
        <div key={index} className={styles.skeleton} />
      ))}
    </div>
  );
}

export function PublicRoutesEmptyState() {
  return (
    <Card className={styles.state}>
      <h3>Маршруты не найдены</h3>
      <p>Попробуйте изменить фильтры, чтобы увидеть доступные публичные маршруты.</p>
    </Card>
  );
}

export function PublicRoutesErrorState({ message, onRetry }: { message: string; onRetry: () => void }) {
  return (
    <Card className={styles.state}>
      <h3>Не удалось загрузить маршруты</h3>
      <p>{message}</p>
      <Button onClick={onRetry}>Повторить</Button>
    </Card>
  );
}
