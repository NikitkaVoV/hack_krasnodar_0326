import { Card } from '@/shared/ui/Card/Card';
import { Spinner } from '@/shared/ui/Spinner/Spinner';
import styles from './DiscoveryStates.module.css';

export function DiscoveryOnboardingState() {
  return (
    <Card className={styles.state}>
      <h3>Выберите область на карте</h3>
      <p>Кликните на карте, чтобы задать центр и начать поиск мест и событий рядом.</p>
    </Card>
  );
}

export function DiscoveryLoadingState() {
  return (
    <div className={styles.loading}>
      <Spinner label="Ищем лучшие места поблизости..." />
      <div className={styles.skeletons}>
        <div />
        <div />
        <div />
      </div>
    </div>
  );
}

export function DiscoveryErrorState({ message }: { message: string }) {
  return (
    <Card className={styles.state}>
      <h3>Не удалось загрузить результаты</h3>
      <p>{message}</p>
    </Card>
  );
}

export function DiscoveryEmptyResultsState() {
  return (
    <Card className={styles.state}>
      <h3>В этом радиусе ничего не найдено</h3>
      <p>Попробуйте увеличить радиус или изменить фильтры, чтобы увидеть больше вариантов.</p>
    </Card>
  );
}
