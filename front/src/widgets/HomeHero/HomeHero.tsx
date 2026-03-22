import { Button } from '@/shared/ui/Button/Button';
import styles from './HomeHero.module.css';

interface HomeHeroProps {
  onPrimaryClick: () => void;
  onSecondaryClick: () => void;
}

export function HomeHero({ onPrimaryClick, onSecondaryClick }: HomeHeroProps) {
  return (
    <section className={styles.hero}>
      <div className={styles.overlay} />
      <div className={styles.inner}>
        <p className={styles.kicker}>Премиальная туристическая платформа</p>
        <h1>Маршруты, события и места для поездки, которая подходит именно вам</h1>
        <p className={styles.subtitle}>
          Исследуйте готовые маршруты, находите актуальные события и получайте персональные рекомендации
          по интересам, дате и формату отдыха.
        </p>
        <div className={styles.actions}>
          <Button onClick={onPrimaryClick}>Собрать маршрут</Button>
          <Button variant="secondary" onClick={onSecondaryClick}>
            Смотреть маршруты
          </Button>
        </div>
      </div>
    </section>
  );
}
