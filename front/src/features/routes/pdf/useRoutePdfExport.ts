import { useState } from 'react';

function isOutdatedOptimizeDepError(error: unknown): boolean {
  if (!error) {
    return false;
  }

  const message =
    error instanceof Error
      ? error.message
      : typeof error === 'object' && 'message' in (error as Record<string, unknown>)
        ? String((error as Record<string, unknown>).message)
        : String(error);

  return message.includes('Outdated Optimize Dep');
}

async function withTimeout<T>(promise: Promise<T>, timeoutMs: number): Promise<T> {
  return await new Promise<T>((resolve, reject) => {
    const timer = window.setTimeout(() => reject(new Error('EXPORT_TIMEOUT')), timeoutMs);

    promise
      .then((value) => {
        window.clearTimeout(timer);
        resolve(value);
      })
      .catch((error) => {
        window.clearTimeout(timer);
        reject(error);
      });
  });
}

export function useRoutePdfExport(routeId: string) {
  const [isExporting, setIsExporting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleExport = async () => {
    if (isExporting) {
      return;
    }

    setError(null);
    setIsExporting(true);

    try {
      const { exportRouteToPdf } = await withTimeout(import('./exportRouteToPdf'), 12000);
      await withTimeout(exportRouteToPdf(routeId), 30000);
    } catch (caughtError) {
      if (isOutdatedOptimizeDepError(caughtError)) {
        setError('PDF-модуль обновился. Обновите страницу (Ctrl+F5) и повторите экспорт.');
      } else if (caughtError instanceof Error && caughtError.message === 'EXPORT_TIMEOUT') {
        setError('Генерация PDF заняла слишком много времени. Попробуйте ещё раз.');
      } else if (caughtError instanceof Error && /Failed to fetch dynamically imported module/i.test(caughtError.message)) {
        setError('Не удалось загрузить модуль PDF. Обновите страницу и попробуйте снова.');
      } else {
        setError('Не удалось сформировать PDF. Попробуйте ещё раз.');
      }
    } finally {
      setIsExporting(false);
    }
  };

  return {
    isExporting,
    error,
    exportPdf: handleExport,
  };
}
