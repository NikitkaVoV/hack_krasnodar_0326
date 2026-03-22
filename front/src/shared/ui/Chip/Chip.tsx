import type { ButtonHTMLAttributes } from 'react';
import styles from './Chip.module.css';
import { cn } from '@/shared/lib/cn';

interface ChipProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  active?: boolean;
}

export function Chip({ active = false, className, ...props }: ChipProps) {
  return <button className={cn(styles.chip, active && styles.active, className)} {...props} />;
}


