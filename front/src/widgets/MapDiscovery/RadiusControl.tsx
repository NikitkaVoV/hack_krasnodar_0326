import styles from './RadiusControl.module.css';

interface RadiusControlProps {
  valueKm: number;
  onChange: (next: number) => void;
}

export function RadiusControl({ valueKm, onChange }: RadiusControlProps) {
  return (
    <section className={styles.root}>
      <div className={styles.topRow}>
        <h3>Радиус поиска</h3>
        <span>{valueKm} км</span>
      </div>
      <input
        className={styles.slider}
        type="range"
        min={1}
        max={50}
        step={1}
        value={valueKm}
        onChange={(event) => onChange(Number(event.target.value))}
        aria-label="Радиус поиска"
      />
      <p>Кликните по карте, затем настройте радиус — результаты обновятся автоматически.</p>
    </section>
  );
}
