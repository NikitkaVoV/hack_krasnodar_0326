import { Button } from '@/shared/ui/Button/Button';
import { EmptyState } from '@/shared/ui/EmptyState/EmptyState';
import { Chip } from '@/shared/ui/Chip/Chip';
import placeholderImage from '@/assets/placeholder.png';
import type { RouteBuilderRecommendation, RouteBuilderRecommendationGroup } from '@/entities/routeBuilder/model';
import styles from './RecommendationSection.module.css';

interface RecommendationSectionProps {
  groups: RouteBuilderRecommendationGroup[];
  isLoading: boolean;
  isError: boolean;
  errorMessage?: string;
  replacingStepSourceId: string | null;
  onAdd: (item: RouteBuilderRecommendation) => void;
  onRetry: () => void;
  onSelect?: (item: RouteBuilderRecommendation) => void;
}

function formatDuration(minutes: number): string {
  if (minutes < 60) {
    return `${minutes} мин`;
  }
  const hours = Math.floor(minutes / 60);
  const rest = minutes % 60;
  return rest ? `${hours} ч ${rest} мин` : `${hours} ч`;
}

export function RecommendationSection({
  groups,
  isLoading,
  isError,
  errorMessage,
  replacingStepSourceId,
  onAdd,
  onRetry,
  onSelect,
}: RecommendationSectionProps) {
  return (
    <section className={styles.root}>
      <header className={styles.header}>
        <h2>Умные рекомендации</h2>
        <p>Подборка адаптируется после каждого шага с учетом времени, радиуса и ограничений.</p>
      </header>

      {isLoading ? <div className={styles.state}>Подбираем лучшие точки...</div> : null}
      {isError ? (
        <div className={styles.state}>
          <p>{errorMessage ?? 'Не удалось загрузить рекомендации.'}</p>
          <Button onClick={onRetry}>Повторить</Button>
        </div>
      ) : null}

      {!isLoading && !isError
        ? groups.map((group) => (
            <article key={group.id} className={styles.group}>
              <header className={styles.groupHeader}>
                <h3>{group.title}</h3>
                <p>{group.subtitle}</p>
              </header>

              {!group.items.length ? (
                <div className={styles.emptyWrap}>
                  <EmptyState title={group.emptyTitle} description={group.emptyDescription} />
                </div>
              ) : (
                <div className={styles.cards}>
                  {group.items.map((item) => (
                    <article
                      className={styles.card}
                      key={item.id}
                      onMouseEnter={() => onSelect?.(item)}
                      onFocus={() => onSelect?.(item)}
                    >
                      <img src={item.imageUrl || placeholderImage} alt={item.title} loading="lazy" />
                      <div className={styles.cardBody}>
                        <div>
                          <p className={styles.type}>{item.type === 'event' ? 'Событие' : 'Место'}</p>
                          <h4>{item.title}</h4>
                          <p className={styles.description}>{item.description}</p>
                        </div>

                        <div className={styles.meta}>
                          <span>{formatDuration(item.estimatedVisitMinutes)}</span>
                          <span>~ {item.travelMinutesFromLast} мин в пути</span>
                          <span>{item.statusLabel}</span>
                        </div>

                        <div className={styles.tags}>
                          {item.tags.slice(0, 3).map((tag) => (
                            <Chip key={tag}>{tag}</Chip>
                          ))}
                        </div>

                        <Button
                          onClick={() => onAdd(item)}
                          variant={replacingStepSourceId ? 'secondary' : 'primary'}
                          fullWidth
                        >
                          {replacingStepSourceId ? 'Заменить шаг' : 'Добавить в маршрут'}
                        </Button>
                      </div>
                    </article>
                  ))}
                </div>
              )}
            </article>
          ))
        : null}
    </section>
  );
}

