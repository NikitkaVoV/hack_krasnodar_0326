import type { CSSProperties } from 'react';
import category1Image from '@/assets/categories/1.png';
import category2Image from '@/assets/categories/4.png';
import category3Image from '@/assets/categories/3.png';
import category4Image from '@/assets/categories/4.png';
import category5Image from '@/assets/categories/5.png';
import type { RouteCollection } from '@/entities/home/model';
import { SectionTitle } from '@/shared/ui/SectionTitle/SectionTitle';
import styles from './CategoriesSection.module.css';

interface CategoriesSectionProps {
  collections: RouteCollection[];
}

const backgroundImages = [category1Image, category2Image, category3Image, category4Image, category5Image];

export function CategoriesSection({ collections }: CategoriesSectionProps) {
  return (
    <section className={styles.section}>
      <SectionTitle title="Коллекции и категории" subtitle="Выберите стиль поездки и сценарий отдыха" />
      <div className={styles.grid}>
        {collections.map((collection, index) => (
          <article
            key={collection.id}
            className={styles.card}
            style={{ '--card-bg-image': `url(${backgroundImages[index % backgroundImages.length]})` } as CSSProperties}
          >
            <span>{collection.key}</span>
            <h3>{collection.title}</h3>
            <p>{collection.description}</p>
          </article>
        ))}
      </div>
    </section>
  );
}
