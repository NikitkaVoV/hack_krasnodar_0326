import type { PublicRoutesFilterState } from '@/entities/publicRoute/model';
import { Chip } from '@/shared/ui/Chip/Chip';
import styles from './RoutesFilters.module.css';

const QUICK = [
  { key: 'weekend', label: 'Выходные' },
  { key: 'family', label: 'Семейные' },
  { key: 'gastro', label: 'Гастро' },
  { key: 'nature', label: 'Природа' },
  { key: 'culture', label: 'Культура' },
] as const;

interface RoutesFiltersProps {
  value: PublicRoutesFilterState;
  onChange: (next: PublicRoutesFilterState) => void;
}

export function RoutesFilters({ value, onChange }: RoutesFiltersProps) {
  return (
    <section className={styles.root}>
      <div className={styles.searchRow}>
        <label>
          <span>Поиск</span>
          <input
            type="search"
            value={value.searchQuery}
            onChange={(event) => onChange({ ...value, searchQuery: event.target.value })}
            placeholder="Название, описание, теги..."
          />
        </label>

        <label>
          <span>Дата</span>
          <input
            type="date"
            value={value.date}
            onChange={(event) => onChange({ ...value, date: event.target.value })}
          />
        </label>
      </div>

      <div className={styles.quickRow}>
        {QUICK.map((item) => (
          <Chip
            key={item.key}
            active={value.quickTags.includes(item.key)}
            onClick={() =>
              onChange({
                ...value,
                quickTags: value.quickTags.includes(item.key)
                  ? value.quickTags.filter((tag) => tag !== item.key)
                  : [...value.quickTags, item.key],
              })
            }
          >
            {item.label}
          </Chip>
        ))}
      </div>

      <div className={styles.selectRow}>
        <label>
          <span>Длительность</span>
          <select
            value={value.duration}
            onChange={(event) => onChange({ ...value, duration: event.target.value as PublicRoutesFilterState['duration'] })}
          >
            <option value="any">Любая</option>
            <option value="short">До 2 часов</option>
            <option value="medium">2-5 часов</option>
            <option value="long">Более 5 часов</option>
          </select>
        </label>

        <label>
          <span>Бюджет</span>
          <select
            value={value.budget}
            onChange={(event) => onChange({ ...value, budget: event.target.value as PublicRoutesFilterState['budget'] })}
          >
            <option value="any">Любой</option>
            <option value="low">До 1500 ₽</option>
            <option value="medium">1500-4000 ₽</option>
            <option value="high">4000+ ₽</option>
          </select>
        </label>

        <label>
          <span>Категория</span>
          <select
            value={value.category}
            onChange={(event) => onChange({ ...value, category: event.target.value as PublicRoutesFilterState['category'] })}
          >
            <option value="all">Все</option>
            <option value="city">Город</option>
            <option value="nature">Природа</option>
            <option value="gastro">Гастро</option>
            <option value="culture">Культура</option>
          </select>
        </label>

        <label>
          <span>Подходит для</span>
          <select
            value={value.suitableFor}
            onChange={(event) =>
              onChange({ ...value, suitableFor: event.target.value as PublicRoutesFilterState['suitableFor'] })
            }
          >
            <option value="all">Всех</option>
            <option value="solo">Соло</option>
            <option value="family">Семьи</option>
            <option value="groups">Группы</option>
          </select>
        </label>

        <label>
          <span>Сортировка</span>
          <select
            value={value.sortBy}
            onChange={(event) => onChange({ ...value, sortBy: event.target.value as PublicRoutesFilterState['sortBy'] })}
          >
            <option value="popular">По популярности</option>
            <option value="duration">По длительности</option>
            <option value="distance">По дистанции</option>
            <option value="budget">По бюджету</option>
          </select>
        </label>
      </div>
    </section>
  );
}
