import { Chip } from '@/shared/ui/Chip/Chip';
import styles from './DiscoveryActions.module.css';

const ACTIONS = [
  { key: 'local', label: 'Исследовать как локал' },
  { key: 'hidden', label: 'Показать скрытые места' },
  { key: 'now', label: 'Что происходит сейчас' },
  { key: 'lucky', label: 'Мне повезёт' },
  { key: 'route', label: 'Построить маршрут отсюда' },
] as const;

interface DiscoveryActionsProps {
  active: string | null;
  onPick: (key: string) => void;
}

export function DiscoveryActions({ active, onPick }: DiscoveryActionsProps) {
  return (
    <section className={styles.root}>
      <h3>Режимы исследования</h3>
      <div className={styles.items}>
        {ACTIONS.map((action) => (
          <Chip key={action.key} active={active === action.key} onClick={() => onPick(action.key)}>
            {action.label}
          </Chip>
        ))}
      </div>
    </section>
  );
}
