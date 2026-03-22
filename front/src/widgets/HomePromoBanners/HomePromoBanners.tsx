import { useNavigate } from 'react-router-dom';
import { appRoutes } from '@/shared/const/routes';
import { Button } from '@/shared/ui/Button/Button';
import styles from './HomePromoBanners.module.css';

export function HomePromoBanners() {
  const navigate = useNavigate();

  return (
    <section className={styles.grid}>
      <article className={styles.bannerA}>
        <h3>РЎРѕР±РµСЂРёС‚Рµ РїРѕРµР·РґРєСѓ РїРѕРґ СЃРµР±СЏ</h3>
        <p>РљРѕРЅСЃС‚СЂСѓРєС‚РѕСЂ РјР°СЂС€СЂСѓС‚Р° РїРѕРјРѕР¶РµС‚ Р·Р°РґР°С‚СЊ С‚РµРјРї, Р±СЋРґР¶РµС‚ Рё Р»СЋР±РёРјС‹Рµ С„РѕСЂРјР°С‚С‹ РѕС‚РґС‹С…Р°.</p>
        <Button onClick={() => navigate(appRoutes.routeBuilder)}>РљРѕРЅСЃС‚СЂСѓРєС‚РѕСЂ РјР°СЂС€СЂСѓС‚Р°</Button>
      </article>
      <article className={styles.bannerB}>
        <h3>РќР°Р№РґРёС‚Рµ РЅРµРѕР±С‹С‡РЅС‹Рµ РјРµСЃС‚Р° Рё СЃРѕР±С‹С‚РёСЏ</h3>
        <p>РСЃСЃР»РµРґСѓР№С‚Рµ Р»РѕРєР°Р»СЊРЅС‹Рµ СЂРµРєРѕРјРµРЅРґР°С†РёРё Рё СЃРµР·РѕРЅРЅС‹Рµ СЃРѕР±С‹С‚РёСЏ РІ РІР°С€РµРј СЂРµРіРёРѕРЅРµ.</p>
        <Button variant="secondary" onClick={() => navigate(appRoutes.routes)}>
          РСЃСЃР»РµРґРѕРІР°С‚СЊ РјР°СЂС€СЂСѓС‚С‹
        </Button>
      </article>
    </section>
  );
}


