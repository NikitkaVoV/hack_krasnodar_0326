import { Button } from '@/shared/ui/Button/Button';
import { EmptyState } from '@/shared/ui/EmptyState/EmptyState';
import type { RouteBuilderStep, RouteBuilderSummary } from '@/entities/routeBuilder/model';
import styles from './RouteStepsPanel.module.css';

interface RouteStepsPanelProps {
  steps: RouteBuilderStep[];
  summary: RouteBuilderSummary;
  selectedStepId: string | null;
  replacingStepSourceId: string | null;
  onSelect: (sourceId: string) => void;
  onRemove: (sourceId: string) => void;
  onMove: (sourceId: string, direction: 'up' | 'down') => void;
  onReplace: (sourceId: string) => void;
  onTogglePin: (sourceId: string) => void;
  onClearAll: () => void;
}

function formatTime(dateTime: string): string {
  const parsed = Date.parse(dateTime);
  if (!Number.isFinite(parsed)) {
    return '--:--';
  }
  return new Date(parsed).toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' });
}

function formatMinutes(minutes: number): string {
  if (minutes < 60) {
    return `${minutes} мин`;
  }

  const hours = Math.floor(minutes / 60);
  const rest = minutes % 60;
  return rest ? `${hours} ч ${rest} мин` : `${hours} ч`;
}

export function RouteStepsPanel({
  steps,
  summary,
  selectedStepId,
  replacingStepSourceId,
  onSelect,
  onRemove,
  onMove,
  onReplace,
  onTogglePin,
  onClearAll,
}: RouteStepsPanelProps) {
  return (
    <section className={styles.root}>
      <header className={styles.header}>
        <div>
          <h2>Текущий маршрут</h2>
          <p>
            {summary.stepsCount} шагов, {formatMinutes(summary.totalPlannedMinutes)} всего
          </p>
        </div>
        <Button variant="ghost" onClick={onClearAll} disabled={!steps.length}>
          Очистить
        </Button>
      </header>

      {!steps.length ? (
        <div className={styles.emptyWrap}>
          <EmptyState
            title="Маршрут пока пуст"
            description="Добавьте первую точку из рекомендаций. После этого появятся тайминг, логичные продолжения и предупреждения."
          />
        </div>
      ) : (
        <div className={styles.list}>
          {steps.map((step, index) => (
            <article
              key={step.sourceId}
              className={`${styles.step} ${selectedStepId === step.sourceId ? styles.selected : ''} ${
                replacingStepSourceId === step.sourceId ? styles.replacing : ''
              }`}
              onClick={() => onSelect(step.sourceId)}
            >
              <div className={styles.order}>{step.order}</div>

              <div className={styles.content}>
                <div className={styles.titleRow}>
                  <h3>{step.title}</h3>
                  <span className={styles.type}>{step.type === 'event' ? 'Событие' : 'Место'}</span>
                </div>

                <p className={styles.meta}>
                  {formatTime(step.plannedStartAt)} - {formatTime(step.plannedEndAt)} · {formatMinutes(step.estimatedVisitMinutes)}
                </p>

                <p className={styles.meta}>Переезд: {step.travelMinutesFromLast} мин · Ожидание: {step.waitMinutes} мин</p>

                <div className={styles.actions}>
                  <Button variant="ghost" onClick={() => onMove(step.sourceId, 'up')} disabled={index === 0}>
                    Выше
                  </Button>
                  <Button
                    variant="ghost"
                    onClick={() => onMove(step.sourceId, 'down')}
                    disabled={index === steps.length - 1}
                  >
                    Ниже
                  </Button>
                  <Button variant="secondary" onClick={() => onReplace(step.sourceId)}>
                    {replacingStepSourceId === step.sourceId ? 'Выберите замену' : 'Заменить'}
                  </Button>
                  <Button variant="ghost" onClick={() => onTogglePin(step.sourceId)}>
                    {step.isPinned ? 'Открепить' : 'Закрепить'}
                  </Button>
                  <Button variant="danger" onClick={() => onRemove(step.sourceId)}>
                    Удалить
                  </Button>
                </div>
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

