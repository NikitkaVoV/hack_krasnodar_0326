import { Button } from '@/shared/ui/Button/Button';
import type { RouteBuilderSummary } from '@/entities/routeBuilder/model';
import styles from './RouteSummary.module.css';

interface RouteSummaryProps {
  summary: RouteBuilderSummary;
  onMakeShorter: () => void;
  onAddFood: () => void;
  onAddEvent: () => void;
}

function formatMinutes(minutes: number): string {
  if (minutes < 60) {
    return `${minutes} мин`;
  }
  const hours = Math.floor(minutes / 60);
  const rest = minutes % 60;
  return rest ? `${hours} ч ${rest} мин` : `${hours} ч`;
}

export function RouteSummary({ summary, onMakeShorter, onAddFood, onAddEvent }: RouteSummaryProps) {
  return (
    <section className={styles.root}>
      <header>
        <h2>Сводка маршрута</h2>
        <p>
          Статус:{' '}
          <span className={summary.status === 'ok' ? styles.ok : summary.status === 'tight' ? styles.tight : styles.overloaded}>
            {summary.status === 'ok' ? 'Сбалансирован' : summary.status === 'tight' ? 'Плотный' : 'Перегружен'}
          </span>
        </p>
      </header>

      <div className={styles.metrics}>
        <div>
          <span>Шагов</span>
          <strong>{summary.stepsCount}</strong>
        </div>
        <div>
          <span>В пути</span>
          <strong>{formatMinutes(summary.totalTravelMinutes)}</strong>
        </div>
        <div>
          <span>На посещения</span>
          <strong>{formatMinutes(summary.totalVisitMinutes)}</strong>
        </div>
        <div>
          <span>Ожидание</span>
          <strong>{formatMinutes(summary.totalWaitMinutes)}</strong>
        </div>
        <div>
          <span>Осталось</span>
          <strong className={summary.remainingMinutes < 0 ? styles.minus : ''}>
            {summary.remainingMinutes < 0 ? '-' : ''}
            {formatMinutes(Math.abs(summary.remainingMinutes))}
          </strong>
        </div>
      </div>

      <div className={styles.quickActions}>
        <Button variant="ghost" onClick={onMakeShorter}>
          Сделать короче
        </Button>
        <Button variant="ghost" onClick={onAddFood}>
          Добавить гастро-стоп
        </Button>
        <Button variant="ghost" onClick={onAddEvent}>
          Добавить событие рядом
        </Button>
      </div>
    </section>
  );
}

