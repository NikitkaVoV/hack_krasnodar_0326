import { Chip } from '@/shared/ui/Chip/Chip';
import styles from './QuickFilters.module.css';

export const QUICK_FILTER_OPTIONS = [
  { key: 'gastronomy', label: 'Гастрономия' },
  { key: 'nature', label: 'Природа' },
  { key: 'active-leisure', label: 'Активный отдых' },
  { key: 'calm-leisure', label: 'Спокойный отдых' },
  { key: 'events-now', label: 'События сейчас' },
  { key: 'family-friendly', label: 'Для семьи' },
  { key: 'hidden-gems', label: 'Нетуристические места' },
  { key: 'popular', label: 'Популярное' },
] as const;

interface QuickFiltersProps {
  selected: string[];
  onToggle: (key: string) => void;
}

export function QuickFilters({ selected, onToggle }: QuickFiltersProps) {
  return (
    <section className={styles.root}>
      <h3>Быстрые фильтры</h3>
      <div className={styles.chips}>
        {QUICK_FILTER_OPTIONS.map((item) => (
          <Chip key={item.key} active={selected.includes(item.key)} onClick={() => onToggle(item.key)}>
            {item.label}
          </Chip>
        ))}
      </div>
    </section>
  );
}
