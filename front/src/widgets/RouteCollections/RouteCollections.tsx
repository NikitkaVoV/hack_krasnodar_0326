import type { RouteCollection } from '@/entities/home/model';
import { SectionTitle } from '@/shared/ui/SectionTitle/SectionTitle';
import styles from './RouteCollections.module.css';

interface RouteCollectionsProps {
  collections: RouteCollection[];
}

export function RouteCollections({ collections }: RouteCollectionsProps) {
  return (
    <section className={styles.section}>
      <SectionTitle title="РџРѕРґР±РѕСЂРєРё РјР°СЂС€СЂСѓС‚РѕРІ" subtitle="Р’С‹Р±РµСЂРёС‚Рµ РЅР°СЃС‚СЂРѕРµРЅРёРµ РїРѕРµР·РґРєРё" />
      <div className={styles.grid}>
        {collections.map((collection) => (
          <article key={collection.id} className={styles.card}>
            <h3>{collection.title}</h3>
            <p>{collection.description}</p>
          </article>
        ))}
      </div>
    </section>
  );
}


