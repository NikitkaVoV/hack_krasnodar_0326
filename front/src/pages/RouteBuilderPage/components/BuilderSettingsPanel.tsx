import { Input } from '@/shared/ui/Input/Input';
import { Select } from '@/shared/ui/Select/Select';
import { Button } from '@/shared/ui/Button/Button';
import type { RouteBuilderSettings } from '@/entities/routeBuilder/model';
import styles from './BuilderSettingsPanel.module.css';

interface BuilderSettingsPanelProps {
  value: RouteBuilderSettings;
  onChange: (updater: (prev: RouteBuilderSettings) => RouteBuilderSettings) => void;
  onBuild: () => void;
  isBuilding: boolean;
}

const TRANSPORT_OPTIONS = [
  { value: 'walk', label: 'Пешком' },
  { value: 'car', label: 'Авто' },
  { value: 'mixed', label: 'Смешанный' },
];

const PACE_OPTIONS = [
  { value: 'calm', label: 'Спокойный' },
  { value: 'active', label: 'Активный' },
  { value: 'family', label: 'Семейный' },
  { value: 'gastro', label: 'Гастро' },
  { value: 'mixed', label: 'Смешанный' },
];

export function BuilderSettingsPanel({ value, onChange, onBuild, isBuilding }: BuilderSettingsPanelProps) {
  return (
    <section className={styles.root}>
      <header className={styles.header}>
        <div>
          <h2>Параметры маршрута</h2>
          <p>Управляйте временем, радиусом и стилем. Рекомендации пересчитываются автоматически.</p>
        </div>
        <Button onClick={onBuild} disabled={isBuilding}>{isBuilding ? 'Обновляем...' : 'Построить маршрут'}</Button>
      </header>

      <div className={styles.grid}>
        <Input
          id="builder-date"
          label="Дата"
          type="date"
          value={value.date}
          onChange={(event) => onChange((prev) => ({ ...prev, date: event.target.value }))}
        />

        <Input
          id="builder-start-time"
          label="Время старта"
          type="time"
          value={value.startTime}
          onChange={(event) => onChange((prev) => ({ ...prev, startTime: event.target.value }))}
        />

        <Input
          id="builder-duration"
          label="Длительность (мин)"
          type="number"
          min={60}
          step={30}
          value={value.totalDurationMinutes}
          onChange={(event) =>
            onChange((prev) => ({
              ...prev,
              totalDurationMinutes: Math.max(60, Number(event.target.value) || 60),
            }))
          }
        />

        <Input
          id="builder-radius"
          label="Радиус (км)"
          type="number"
          min={1}
          max={60}
          value={value.radiusKm}
          onChange={(event) =>
            onChange((prev) => ({
              ...prev,
              radiusKm: Math.max(1, Math.min(60, Number(event.target.value) || 1)),
            }))
          }
        />

        <Select
          id="builder-transport"
          label="Транспорт"
          options={TRANSPORT_OPTIONS}
          value={value.transportMode}
          onChange={(event) => onChange((prev) => ({ ...prev, transportMode: event.target.value as RouteBuilderSettings['transportMode'] }))}
        />

        <Select
          id="builder-pace"
          label="Стиль маршрута"
          options={PACE_OPTIONS}
          value={value.pacePreset}
          onChange={(event) => onChange((prev) => ({ ...prev, pacePreset: event.target.value as RouteBuilderSettings['pacePreset'] }))}
        />

        <Input
          id="builder-manual-search"
          label="Поиск точки вручную"
          placeholder="Например: кофе, музей, парк"
          value={value.manualSearch}
          onChange={(event) => onChange((prev) => ({ ...prev, manualSearch: event.target.value }))}
        />
      </div>

      <div className={styles.toggles}>
        <label className={styles.toggle}>
          <input
            type="checkbox"
            checked={value.onlyOpenPlaces}
            onChange={(event) => onChange((prev) => ({ ...prev, onlyOpenPlaces: event.target.checked }))}
          />
          <span>Только открытые места</span>
        </label>

        <label className={styles.toggle}>
          <input
            type="checkbox"
            checked={value.includeEvents}
            onChange={(event) => onChange((prev) => ({ ...prev, includeEvents: event.target.checked }))}
          />
          <span>Добавлять события</span>
        </label>
      </div>
    </section>
  );
}

