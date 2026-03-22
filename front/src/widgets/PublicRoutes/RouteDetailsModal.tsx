import { useCallback, useEffect, useMemo, useState } from 'react';
import type { PublicRouteDetails } from '@/entities/publicRoute/model';
import { RouteInfoPanel } from './RouteInfoPanel';
import { RoutePointsList } from './RoutePointsList';
import { RouteMap } from './RouteMap';
import { RoutePointDetailsCard } from './RoutePointDetailsCard';
import styles from './RouteDetailsModal.module.css';

interface RouteDetailsModalProps {
  isOpen: boolean;
  route: PublicRouteDetails | null;
  isLoading: boolean;
  onClose: () => void;
}

export function RouteDetailsModal({ isOpen, route, isLoading, onClose }: RouteDetailsModalProps) {
  const [selectedPointId, setSelectedPointId] = useState<string | null>(null);
  const [mobileTab, setMobileTab] = useState<'list' | 'map'>('list');

  const availablePointIds = useMemo(() => new Set((route?.points ?? []).map((point) => point.id)), [route?.points]);

  useEffect(() => {
    if (!isOpen) {
      return;
    }
    setSelectedPointId(route?.points[0]?.id ?? null);
    setMobileTab('list');
  }, [isOpen, route?.id]);

  const handleSelectPoint = useCallback(
    (pointId: string) => {
      if (!pointId || !availablePointIds.has(pointId)) {
        return;
      }
      setSelectedPointId(pointId);
    },
    [availablePointIds],
  );

  const selectedPoint = useMemo(
    () => route?.points.find((point) => point.id === selectedPointId) ?? route?.points[0] ?? null,
    [route?.points, selectedPointId],
  );

  if (!isOpen) {
    return null;
  }

  return (
    <div className={styles.overlay}>
      <section className={styles.modal} role="dialog" aria-modal="true" aria-label="Детали маршрута">
        <button type="button" className={styles.close} onClick={onClose} aria-label="Закрыть">
          ×
        </button>

        {isLoading ? <div className={styles.loading}>Загружаем детали маршрута...</div> : null}
        {!isLoading && !route ? <div className={styles.loading}>Не удалось загрузить детали маршрута.</div> : null}

        {!isLoading && route ? (
          <>
            <div className={styles.mobileTabs}>
              <button
                type="button"
                className={mobileTab === 'list' ? styles.activeTab : ''}
                onClick={() => setMobileTab('list')}
              >
                Список
              </button>
              <button
                type="button"
                className={mobileTab === 'map' ? styles.activeTab : ''}
                onClick={() => setMobileTab('map')}
              >
                Карта
              </button>
            </div>

            <div className={styles.layout}>
              <div className={`${styles.left} ${mobileTab === 'map' ? styles.hideOnMobile : ''}`}>
                <RouteInfoPanel route={route} />
                <RoutePointsList
                  points={route.points}
                  selectedPointId={selectedPoint?.id ?? null}
                  onSelectPoint={handleSelectPoint}
                />
              </div>

              <div className={`${styles.right} ${mobileTab === 'list' ? styles.hideOnMobile : ''}`}>
                <RouteMap
                  points={route.points}
                  selectedPointId={selectedPoint?.id ?? null}
                  onSelectPoint={handleSelectPoint}
                />
                <RoutePointDetailsCard point={selectedPoint} />
              </div>
            </div>
          </>
        ) : null}
      </section>
    </div>
  );
}
