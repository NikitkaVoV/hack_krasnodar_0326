import type { EventCard } from '@/entities/home/model';
import { SectionTitle } from '@/shared/ui/SectionTitle/SectionTitle';
import { Card } from '@/shared/ui/Card/Card';
import { EmptyState } from '@/shared/ui/EmptyState/EmptyState';
import { formatDate } from '@/shared/lib/format';
import styles from './EventsSection.module.css';

interface EventsSectionProps {
  events: EventCard[];
}

export function EventsSection({ events }: EventsSectionProps) {
  return (
    <section className={styles.section}>
      <SectionTitle title="Ближайшие события" subtitle="Подберите поездку вокруг интересных мероприятий" />
      {!events.length ? (
        <EmptyState
          title="Событий пока нет"
          description="Измените дату поездки или вернитесь позже, когда появятся новые анонсы."
        />
      ) : (
        <div className={styles.grid}>
          {events.map((event) => (
            <Card key={event.id} className={styles.card}>
              <p className={styles.time}>{event.startAt ? formatDate(event.startAt) : 'Дата уточняется'}</p>
              <h3>{event.name}</h3>
              <p className={styles.description}>{event.description}</p>
              <p className={styles.location}>{event.location}</p>
            </Card>
          ))}
        </div>
      )}
    </section>
  );
}
