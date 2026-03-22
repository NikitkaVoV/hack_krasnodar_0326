import type { HomeEventCard } from '@/entities/home/model';
import { SectionTitle } from '@/shared/ui/SectionTitle/SectionTitle';
import { Card } from '@/shared/ui/Card/Card';
import { EmptyState } from '@/shared/ui/EmptyState/EmptyState';
import { formatDate } from '@/shared/lib/format';
import styles from './UpcomingEventsSection.module.css';

interface UpcomingEventsSectionProps {
  events: HomeEventCard[];
}

export function UpcomingEventsSection({ events }: UpcomingEventsSectionProps) {
  return (
    <section className={styles.section}>
      <SectionTitle title="Р‘Р»РёР¶Р°Р№С€РёРµ СЃРѕР±С‹С‚РёСЏ" subtitle="Р’С‹СЃС‚Р°РІРєРё, С„РµСЃС‚РёРІР°Р»Рё Рё СЃРµР·РѕРЅРЅС‹Рµ Р°РєС‚РёРІРЅРѕСЃС‚Рё" />
      {!events.length ? (
        <EmptyState
          title="РЎРѕР±С‹С‚РёР№ РїРѕРєР° РЅРµС‚"
          description="Р”РѕР±Р°РІСЊС‚Рµ РґР°С‚С‹ РїРѕРµР·РґРєРё РїРѕР·Р¶Рµ, РјС‹ РїРѕРєР°Р¶РµРј РїРѕРґС…РѕРґСЏС‰РёРµ СЃРѕР±С‹С‚РёСЏ."
        />
      ) : (
        <div className={styles.grid}>
          {events.map((event) => (
            <Card key={event.id} className={styles.card}>
              <p className={styles.date}>{event.startAt ? formatDate(event.startAt) : 'Р”Р°С‚Р° СѓС‚РѕС‡РЅСЏРµС‚СЃСЏ'}</p>
              <h3>{event.name}</h3>
              <p>{event.description}</p>
              <p className={styles.location}>{event.location}</p>
            </Card>
          ))}
        </div>
      )}
    </section>
  );
}


