import placeholderImage from '@/assets/placeholder.png';
import type { MapEntity } from '@/entities/map/model';
import { badgeLabel, openStatusLabel } from './presentation';
import { useImageWithPlaceholder } from './useImageWithPlaceholder';
import styles from './ResultCard.module.css';

interface ResultCardProps {
  item: MapEntity;
  isSelected: boolean;
  onClick: () => void;
  onHover: () => void;
}

export function ResultCard({ item, isSelected, onClick, onHover }: ResultCardProps) {
  const imageSrc = useImageWithPlaceholder(item.imageUrl, placeholderImage);

  return (
    <article
      className={`${styles.card} ${isSelected ? styles.selected : ''}`}
      onClick={onClick}
      onMouseEnter={onHover}
      role="button"
      tabIndex={0}
      onKeyDown={(event) => {
        if (event.key === 'Enter' || event.key === ' ') {
          event.preventDefault();
          onClick();
        }
      }}
    >
      <div className={styles.imageWrap}>
        <img className={styles.image} src={imageSrc} alt={item.title} loading="lazy" />
      </div>
      <div className={styles.content}>
        <div className={styles.main}>
          <div className={styles.topRow}>
            <span className={styles.type}>{item.type === 'event' ? 'Событие' : 'Место'}</span>
            <span className={styles.distance}>{item.distanceKm.toFixed(1)} км</span>
          </div>
          <h3 className={styles.title}>{item.title}</h3>
          <p className={styles.description}>{item.description}</p>
        </div>
        <div className={styles.bottom}>
          <div className={styles.badges}>
            {item.badges.slice(0, 3).map((badge) => (
              <span key={badge}>{badgeLabel(badge)}</span>
            ))}
          </div>

          {item.type === 'event' ? (
            <p className={styles.meta}>Начало: {new Date(item.eventStartAt).toLocaleString('ru-RU')}</p>
          ) : (
            <p className={styles.meta}>{openStatusLabel(item.isOpenNow)}</p>
          )}
        </div>
      </div>
    </article>
  );
}
