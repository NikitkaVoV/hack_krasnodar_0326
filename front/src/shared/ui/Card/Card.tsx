import type { PropsWithChildren } from 'react';
import styles from './Card.module.css';
import { cn } from '@/shared/lib/cn';

interface CardProps extends PropsWithChildren {
  className?: string;
}

export function Card({ children, className }: CardProps) {
  return <section className={cn(styles.card, className)}>{children}</section>;
}


