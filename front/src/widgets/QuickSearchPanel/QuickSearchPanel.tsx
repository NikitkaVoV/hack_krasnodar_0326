import { useMemo, useState } from 'react';
import { Button } from '@/shared/ui/Button/Button';
import { Chip } from '@/shared/ui/Chip/Chip';
import { Input } from '@/shared/ui/Input/Input';
import { Select } from '@/shared/ui/Select/Select';
import styles from './QuickSearchPanel.module.css';

const quickOptions = [
  { key: 'weekend', label: 'Выходные' },
  { key: 'family', label: 'Семейные' },
  { key: 'gastro', label: 'Гастро' },
  { key: 'nature', label: 'Природа' },
  { key: 'culture', label: 'Культура' },
] as const;

interface QuickSearchPanelSubmitPayload {
  query: string;
  date: string;
  duration: 'any' | 'short' | 'medium' | 'long';
  quickTags: string[];
}

interface QuickSearchPanelProps {
  onSubmit: (payload: QuickSearchPanelSubmitPayload) => void;
}

export function QuickSearchPanel({ onSubmit }: QuickSearchPanelProps) {
  const [query, setQuery] = useState('');
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10));
  const [duration, setDuration] = useState<'any' | 'short' | 'medium' | 'long'>('any');
  const [quickTags, setQuickTags] = useState<string[]>([]);

  const durationOptions = useMemo(
    () => [
      { value: 'any', label: 'Любая' },
      { value: 'short', label: 'До 2 часов' },
      { value: 'medium', label: '2-5 часов' },
      { value: 'long', label: 'Более 5 часов' },
    ],
    [],
  );

  const toggleQuickTag = (tag: string) => {
    setQuickTags((prev) => (prev.includes(tag) ? prev.filter((item) => item !== tag) : [...prev, tag]));
  };

  return (
    <section className={styles.panel}>
      <div className={styles.grid}>
        <Input
          id="quick-search-query"
          label="Интересы / что хотите"
          placeholder="Природа, история, локальная кухня, события..."
          value={query}
          onChange={(event) => setQuery(event.target.value)}
        />

        <label className={styles.field}>
          <span>Дата</span>
          <input type="date" value={date} onChange={(event) => setDate(event.target.value)} />
        </label>

        <Select
          id="quick-search-duration"
          label="Длительность"
          value={duration}
          onChange={(event) => setDuration(event.target.value as 'any' | 'short' | 'medium' | 'long')}
          options={durationOptions}
        />
      </div>

      <div className={styles.bottom}>
        <div className={styles.filters}>
          {quickOptions.map((item) => (
            <Chip key={item.key} active={quickTags.includes(item.key)} onClick={() => toggleQuickTag(item.key)}>
              {item.label}
            </Chip>
          ))}
        </div>

        <Button
          onClick={() =>
            onSubmit({
              query: query.trim(),
              date,
              duration,
              quickTags,
            })
          }
          className={styles.submit}
        >
          Подобрать маршрут
        </Button>
      </div>
    </section>
  );
}
