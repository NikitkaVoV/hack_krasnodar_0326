import { Card } from '@/shared/ui/Card/Card';
import styles from './EmptyState.module.css';

interface EmptyStateProps {
  title: string;
  description: string;
}

export function EmptyState({ title, description }: EmptyStateProps) {
  return (
    <Card className={styles.root}>
      <h3>{title}</h3>
      <p>{description}</p>
    </Card>
  );
}


