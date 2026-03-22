import { useState } from 'react';
import { Button } from '@/shared/ui/Button/Button';
import { Chip } from '@/shared/ui/Chip/Chip';
import { Input } from '@/shared/ui/Input/Input';
import { Select } from '@/shared/ui/Select/Select';
import styles from './QuickRouteSearch.module.css';

const travelFormats = ['РџРµС€РёР№', 'РЎРµРјРµР№РЅС‹Р№', 'РЎРїРѕРєРѕР№РЅС‹Р№', 'РђРєС‚РёРІРЅС‹Р№'];

interface QuickRouteSearchProps {
  onSubmit: (payload: { query: string; date: string; duration: string; format: string; pace: string }) => void;
}

export function QuickRouteSearch({ onSubmit }: QuickRouteSearchProps) {
  const [query, setQuery] = useState('');
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10));
  const [duration, setDuration] = useState('half-day');
  const [format, setFormat] = useState('РџРµС€РёР№');
  const [pace, setPace] = useState('РљРѕРјС„РѕСЂС‚РЅС‹Р№');

  return (
    <section className={styles.panel}>
      <div className={styles.grid}>
        <Input
          id="quick-query"
          label="Р§С‚Рѕ РІР°Рј РёРЅС‚РµСЂРµСЃРЅРѕ?"
          placeholder="РџР°СЂРєРё, РіР°СЃС‚СЂРѕ-С‚РѕС‡РєРё, РёСЃС‚РѕСЂРёСЏ, СЃРѕР±С‹С‚РёСЏ..."
          value={query}
          onChange={(event) => setQuery(event.target.value)}
        />
        <label className={styles.dateField}>
          <span>Р”Р°С‚Р°</span>
          <input type="date" value={date} onChange={(event) => setDate(event.target.value)} />
        </label>
        <Select
          id="quick-duration"
          label="Р”Р»РёС‚РµР»СЊРЅРѕСЃС‚СЊ"
          value={duration}
          onChange={(event) => setDuration(event.target.value)}
          options={[
            { value: 'short', label: '2-4 С‡Р°СЃР°' },
            { value: 'half-day', label: 'РџРѕР»РґРЅСЏ' },
            { value: 'full-day', label: 'Р¦РµР»С‹Р№ РґРµРЅСЊ' },
            { value: 'weekend', label: 'Р’С‹С…РѕРґРЅС‹Рµ' },
          ]}
        />
        <Select
          id="quick-pace"
          label="РўРµРјРї"
          value={pace}
          onChange={(event) => setPace(event.target.value)}
          options={[
            { value: 'РљРѕРјС„РѕСЂС‚РЅС‹Р№', label: 'РљРѕРјС„РѕСЂС‚РЅС‹Р№' },
            { value: 'РРЅС‚РµРЅСЃРёРІРЅС‹Р№', label: 'РРЅС‚РµРЅСЃРёРІРЅС‹Р№' },
            { value: 'Р Р°СЃСЃР»Р°Р±Р»РµРЅРЅС‹Р№', label: 'Р Р°СЃСЃР»Р°Р±Р»РµРЅРЅС‹Р№' },
          ]}
        />
      </div>
      <div className={styles.bottom}>
        <div className={styles.chips}>
          {travelFormats.map((item) => (
            <Chip key={item} active={format === item} onClick={() => setFormat(item)}>
              {item}
            </Chip>
          ))}
        </div>
        <Button
          onClick={() =>
            onSubmit({
              query,
              date,
              duration,
              format,
              pace,
            })
          }
        >
          РџРѕРґРѕР±СЂР°С‚СЊ РјР°СЂС€СЂСѓС‚
        </Button>
      </div>
    </section>
  );
}


