import type { RouteBuilderWarning } from '@/entities/routeBuilder/model';
import styles from './RouteWarnings.module.css';

interface RouteWarningsProps {
  items: RouteBuilderWarning[];
}

export function RouteWarnings({ items }: RouteWarningsProps) {
  return (
    <section className={styles.root}>
      <header>
        <h2>Проверка маршрута</h2>
        <p>Сигналы о конфликтах времени, перегрузке и ограничениях.</p>
      </header>

      {!items.length ? (
        <div className={styles.ok}>Критичных конфликтов не найдено. Маршрут выглядит согласованным.</div>
      ) : (
        <ul className={styles.list}>
          {items.map((warning) => (
            <li key={warning.id} className={warning.level === 'critical' ? styles.critical : styles.warning}>
              <strong>{warning.title}</strong>
              <p>{warning.message}</p>
              {typeof warning.stepOrder === 'number' ? <span>Шаг {warning.stepOrder}</span> : null}
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}

