import { useEffect, useRef } from 'react';
import type { MapEntity } from '@/entities/map/model';
import { ResultCard } from './ResultCard';
import styles from './ResultList.module.css';

interface ResultListProps {
  items: MapEntity[];
  selectedId: string | null;
  mode: 'carousel' | 'list';
  onSelect: (id: string) => void;
  onHover: (id: string) => void;
}

export function ResultList({ items, selectedId, mode, onSelect, onHover }: ResultListProps) {
  const nodesRef = useRef<Record<string, HTMLDivElement | null>>({});

  useEffect(() => {
    if (!selectedId) {
      return;
    }

    const node = nodesRef.current[selectedId];
    if (!node) {
      return;
    }

    node.scrollIntoView({ behavior: 'smooth', block: 'nearest', inline: 'center' });
  }, [selectedId]);

  return (
    <div className={mode === 'carousel' ? styles.carousel : styles.list}>
      {items.map((item) => (
        <div key={item.id} ref={(element) => { nodesRef.current[item.id] = element; }}>
          <ResultCard
            item={item}
            isSelected={selectedId === item.id}
            onClick={() => onSelect(item.id)}
            onHover={() => onHover(item.id)}
          />
        </div>
      ))}
    </div>
  );
}
