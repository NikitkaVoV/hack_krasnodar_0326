import styles from './RoutesHeader.module.css';

interface RoutesHeaderProps {
  total: number;
}

export function RoutesHeader({ total }: RoutesHeaderProps) {
  return (
    <section className={styles.root}>
      <p className={styles.kicker}>Public Routes</p>
      <h1>Готовые маршруты</h1>
      <p>
        Готовые публичные маршруты от платформы: выбирайте сценарий, открывайте детали и проходите
        точки в удобном темпе.
      </p>
      <div className={styles.stats}>
        <span>{total} маршрутов</span>
        <span>Кураторские подборки</span>
        <span>Карта + план по точкам</span>
      </div>
    </section>
  );
}
