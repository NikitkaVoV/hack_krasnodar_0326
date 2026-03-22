import { Chip } from '@/shared/ui/Chip/Chip';
import { Input } from '@/shared/ui/Input/Input';
import { Select } from '@/shared/ui/Select/Select';
import styles from './SearchFilters.module.css';

const filterTypes = ['РџСЂРёСЂРѕРґР°', 'РњСѓР·РµРё', 'Р“Р°СЃС‚СЂРѕ', 'РСЃС‚РѕСЂРёСЏ', 'РЎРµРјРµР№РЅС‹Р№ С„РѕСЂРјР°С‚'];

interface SearchFiltersProps {
  search: string;
  onSearchChange: (value: string) => void;
  activeType: string;
  onTypeChange: (value: string) => void;
  duration: string;
  onDurationChange: (value: string) => void;
}

export function SearchFilters({
  search,
  onSearchChange,
  activeType,
  onTypeChange,
  duration,
  onDurationChange,
}: SearchFiltersProps) {
  return (
    <section className={styles.root}>
      <Input
        id="search"
        label="РРЅС‚РµСЂРµСЃС‹ РёР»Рё Р»РѕРєР°С†РёСЏ"
        placeholder="РќР°РїСЂРёРјРµСЂ: С†РµРЅС‚СЂ, РїР°СЂРєРё, РјСѓР·РµРё"
        value={search}
        onChange={(event) => onSearchChange(event.target.value)}
      />
      <div className={styles.chips}>
        {filterTypes.map((type) => (
          <Chip key={type} active={activeType === type} onClick={() => onTypeChange(type)}>
            {type}
          </Chip>
        ))}
        <Chip active={!activeType} onClick={() => onTypeChange('')}>
          Р’СЃРµ
        </Chip>
      </div>
      <Select
        id="duration"
        label="РџСЂРѕРґРѕР»Р¶РёС‚РµР»СЊРЅРѕСЃС‚СЊ"
        value={duration}
        onChange={(event) => onDurationChange(event.target.value)}
        options={[
          { value: 'any', label: 'Р›СЋР±Р°СЏ' },
          { value: 'short', label: 'Р”Рѕ 4 С‡Р°СЃРѕРІ' },
          { value: 'middle', label: '4-8 С‡Р°СЃРѕРІ' },
          { value: 'long', label: 'Р¦РµР»С‹Р№ РґРµРЅСЊ' },
        ]}
      />
    </section>
  );
}


