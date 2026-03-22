import type { PropsWithChildren } from 'react';
import styles from './PageContainer.module.css';
import { cn } from '@/shared/lib/cn';

interface PageContainerProps extends PropsWithChildren {
  className?: string;
}

export function PageContainer({ children, className }: PageContainerProps) {
  return <div className={cn(styles.container, className)}>{children}</div>;
}


