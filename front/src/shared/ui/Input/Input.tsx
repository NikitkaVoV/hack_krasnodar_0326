import type { InputHTMLAttributes } from 'react';
import styles from './Input.module.css';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  error?: string;
}

export function Input({ label, id, error, ...props }: InputProps) {
  return (
    <label className={styles.root} htmlFor={id}>
      <span className={styles.label}>{label}</span>
      <input id={id} className={styles.input} {...props} />
      {error ? <span className={styles.error}>{error}</span> : null}
    </label>
  );
}


