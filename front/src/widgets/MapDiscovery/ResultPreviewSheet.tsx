import placeholderImage from '@/assets/placeholder.png';
import { Button } from '@/shared/ui/Button/Button';
import type { MapEntity } from '@/entities/map/model';
import { categoryLabel, localizePracticalInfo, openStatusLabel } from './presentation';
import { useImageWithPlaceholder } from './useImageWithPlaceholder';
import styles from './ResultPreviewSheet.module.css';

interface ResultPreviewSheetProps {
  item: MapEntity | null;
  onClose: () => void;
}

export function ResultPreviewSheet({ item, onClose }: ResultPreviewSheetProps) {
  const imageSrc = useImageWithPlaceholder(item?.imageUrl, placeholderImage);

  if (!item) {
    return null;
  }

  return (
    <aside className={styles.sheet}>
      <button className={styles.close} onClick={onClose} aria-label="Закрыть превью">
        ×
      </button>
      <div className={styles.imageWrap}>
        <img className={styles.image} src={imageSrc} alt={item.title} loading="lazy" />
      </div>
      <div className={styles.content}>
        <h3>{item.title}</h3>
        <p>{item.description}</p>
        <div className={styles.meta}>
          <span>{item.type === 'event' ? 'Событие' : 'Место'}</span>
          <span>{item.distanceKm.toFixed(1)} км</span>
          <span>{categoryLabel(item.category)}</span>
        </div>

        {item.type === 'place' ? (
          <p className={styles.extra}>{localizePracticalInfo(item.practicalInfo)}</p>
        ) : (
          <p className={styles.extra}>Начало: {new Date(item.eventStartAt).toLocaleString('ru-RU')}</p>
        )}
        <p className={styles.extra}>{openStatusLabel(item.isOpenNow)}</p>

        <div className={styles.actions}>
          <Button>Подробнее</Button>
          <Button variant="ghost">В избранное</Button>
          <Button variant="ghost">Построить маршрут</Button>
          <Button variant="secondary">Рядом</Button>
        </div>
      </div>
    </aside>
  );
}
