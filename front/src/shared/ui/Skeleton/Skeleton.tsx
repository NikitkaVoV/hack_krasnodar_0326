import styles from './Skeleton.module.css';

interface SkeletonProps {
  height?: number;
}

export function Skeleton({ height = 20 }: SkeletonProps) {
  return <div className={styles.skeleton} style={{ height }} />;
}


