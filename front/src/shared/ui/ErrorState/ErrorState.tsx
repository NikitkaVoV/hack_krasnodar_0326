import { Card } from '@/shared/ui/Card/Card';
import styles from './ErrorState.module.css';

interface ErrorStateProps {
  title?: string;
  message: string;
}

export function ErrorState({ title = 'Ошибка', message }: ErrorStateProps) {
  return (
    <Card className={styles.root}>
      <h3>{title}</h3>
      <p>{message}</p>
    </Card>
  );
}
