import type { ButtonHTMLAttributes } from 'react';
import styles from './Button.module.css';
import { cn } from '@/shared/lib/cn';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger';
  fullWidth?: boolean;
}

export function Button({
  variant = 'primary',
  fullWidth = false,
  className,
  ...props
}: ButtonProps) {
  return (
    <button
      className={cn(styles.button, styles[variant], fullWidth && styles.fullWidth, className)}
      {...props}
    />
  );
}


