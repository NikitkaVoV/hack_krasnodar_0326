import { Button } from '@/shared/ui/Button/Button';
import { useRoutePdfExport } from '../useRoutePdfExport';
import styles from './RoutePdfExportButton.module.css';

interface RoutePdfExportButtonProps {
  routeId: string;
  fullWidth?: boolean;
  compact?: boolean;
}

export function RoutePdfExportButton({ routeId, fullWidth = false, compact = false }: RoutePdfExportButtonProps) {
  const { isExporting, error, exportPdf } = useRoutePdfExport(routeId);

  return (
    <div className={styles.root}>
      <Button
        variant={compact ? 'ghost' : 'secondary'}
        onClick={(event) => {
          event.stopPropagation();
          void exportPdf();
        }}
        disabled={isExporting}
        fullWidth={fullWidth}
      >
        {isExporting ? 'Генерация PDF...' : compact ? 'Скачать PDF' : 'Экспорт в PDF'}
      </Button>

      {error ? <span className={styles.error}>{error}</span> : null}
    </div>
  );
}
