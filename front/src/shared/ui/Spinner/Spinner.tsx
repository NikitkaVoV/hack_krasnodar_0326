import styles from './Spinner.module.css';

interface SpinnerProps {
  label?: string;
  fullScreen?: boolean;
}

export function Spinner({ label = 'Загрузка...', fullScreen = false }: SpinnerProps) {
  return (
    <div className={fullScreen ? styles.fullScreen : styles.root} role="status" aria-live="polite">
      <div className={styles.dot} />
      <span>{label}</span>
    </div>
  );
}
