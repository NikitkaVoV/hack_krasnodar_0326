import type { FilterState } from '@/entities/map/model';
import styles from './AdvancedFilters.module.css';

interface AdvancedFiltersProps {
  value: FilterState;
  onChange: (next: FilterState) => void;
}

function toggleSuitable(
  selected: FilterState['suitableFor'],
  target: FilterState['suitableFor'][number],
): FilterState['suitableFor'] {
  return selected.includes(target) ? selected.filter((item) => item !== target) : [...selected, target];
}

export function AdvancedFilters({ value, onChange }: AdvancedFiltersProps) {
  return (
    <details className={styles.root}>
      <summary>Расширенные фильтры</summary>

      <div className={styles.grid}>
        <label>
          <span>Тип сущностей</span>
          <select
            value={value.entityType}
            onChange={(event) => onChange({ ...value, entityType: event.target.value as FilterState['entityType'] })}
          >
            <option value="both">Места и события</option>
            <option value="places">Только места</option>
            <option value="events">Только события</option>
          </select>
        </label>

        <label>
          <span>Мин. бюджет (₽)</span>
          <input
            type="number"
            value={value.minBudget ?? ''}
            onChange={(event) =>
              onChange({
                ...value,
                minBudget: event.target.value ? Number(event.target.value) : undefined,
              })
            }
          />
        </label>

        <label>
          <span>Макс. бюджет (₽)</span>
          <input
            type="number"
            value={value.maxBudget ?? ''}
            onChange={(event) =>
              onChange({
                ...value,
                maxBudget: event.target.value ? Number(event.target.value) : undefined,
              })
            }
          />
        </label>

        <label>
          <span>Длительность</span>
          <select
            value={value.duration ?? 'any'}
            onChange={(event) =>
              onChange({
                ...value,
                duration: event.target.value as FilterState['duration'],
              })
            }
          >
            <option value="any">Любая</option>
            <option value="short">Короткая</option>
            <option value="half-day">Полдня</option>
            <option value="full-day">Целый день</option>
          </select>
        </label>

        <label>
          <span>Дата</span>
          <input
            type="date"
            value={value.date ?? ''}
            onChange={(event) => onChange({ ...value, date: event.target.value || undefined })}
          />
        </label>

        <label>
          <span>Время</span>
          <input
            type="time"
            value={value.time ?? ''}
            onChange={(event) => onChange({ ...value, time: event.target.value || undefined })}
          />
        </label>

        <label>
          <span>Сортировка</span>
          <select
            value={value.sortBy}
            onChange={(event) => onChange({ ...value, sortBy: event.target.value as FilterState['sortBy'] })}
          >
            <option value="distance">По дистанции</option>
            <option value="popularity">По популярности</option>
            <option value="relevance">По релевантности</option>
            <option value="starting-soon">Скоро начнётся</option>
          </select>
        </label>
      </div>

      <div className={styles.flags}>
        <label className={styles.checkbox}>
          <input
            type="checkbox"
            checked={value.openNow}
            onChange={(event) => onChange({ ...value, openNow: event.target.checked })}
          />
          <span>Открыто сейчас</span>
        </label>

        <div className={styles.suitableFor}>
          <p>Подходит для:</p>
          <div>
            {[
              { key: 'children', label: 'Детей' },
              { key: 'elderly', label: 'Пожилых' },
              { key: 'solo', label: 'Соло' },
              { key: 'groups', label: 'Групп' },
            ].map((item) => (
              <label key={item.key} className={styles.checkbox}>
                <input
                  type="checkbox"
                  checked={value.suitableFor.includes(item.key as FilterState['suitableFor'][number])}
                  onChange={() =>
                    onChange({
                      ...value,
                      suitableFor: toggleSuitable(
                        value.suitableFor,
                        item.key as FilterState['suitableFor'][number],
                      ),
                    })
                  }
                />
                <span>{item.label}</span>
              </label>
            ))}
          </div>
        </div>
      </div>
    </details>
  );
}
