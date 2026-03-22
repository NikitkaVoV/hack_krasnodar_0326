import type { AppRouteStep } from '@/entities/route/model';
import type { RouteTargetModel } from '@/entities/event/model';
import { Card } from '@/shared/ui/Card/Card';
import styles from './RouteStepList.module.css';
import { formatMinutes } from '@/shared/lib/format';

interface RouteStepListProps {
  steps: AppRouteStep[];
  targets: Map<string, RouteTargetModel>;
}

export function RouteStepList({ steps, targets }: RouteStepListProps) {
  return (
    <div className={styles.list}>
      {steps.map((step) => {
        const target = targets.get(step.targetId);
        return (
          <Card key={`${step.targetId}-${step.stepOrder}`} className={styles.item}>
            <h3>
              РЁР°Рі {step.stepOrder}: {target?.name ?? 'РћР±СЉРµРєС‚ РјР°СЂС€СЂСѓС‚Р°'}
            </h3>
            <p>{target?.description ?? 'Р”РµС‚Р°Р»Рё РЅРµРґРѕСЃС‚СѓРїРЅС‹'}</p>
            <div className={styles.meta}>
              <span>
                Р’СЂРµРјСЏ: {step.plannedTimeStart} - {step.plannedTimeEnd}
              </span>
              <span>РўСЂР°РЅСЃРїРѕСЂС‚: {step.transportMode || 'РќРµ СѓРєР°Р·Р°РЅ'}</span>
              <span>РџРµСЂРµРµР·Рґ: {formatMinutes(step.travelTimeMinutes)}</span>
              <span>РћР¶РёРґР°РЅРёРµ: {formatMinutes(step.waitTimeMinutes)}</span>
              <span>Р›РѕРєР°С†РёСЏ: {target?.location ?? 'РќРµРёР·РІРµСЃС‚РЅРѕ'}</span>
            </div>
            {step.notes ? <p className={styles.notes}>РџСЂРёРјРµС‡Р°РЅРёРµ: {step.notes}</p> : null}
          </Card>
        );
      })}
    </div>
  );
}


