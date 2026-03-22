import styles from './HeroBanner.module.css';
import { Button } from '@/shared/ui/Button/Button';
import { useNavigate } from 'react-router-dom';
import { appRoutes } from '@/shared/const/routes';

export function HeroBanner() {
  const navigate = useNavigate();
  return (
    <section className={styles.banner}>
      <div>
        <h1>РЈРјРЅС‹Рµ С‚СѓСЂРёСЃС‚РёС‡РµСЃРєРёРµ РјР°СЂС€СЂСѓС‚С‹</h1>
        <p>РџРѕРґР±РµСЂРёС‚Рµ РјР°СЂС€СЂСѓС‚ РїРѕ РёРЅС‚РµСЂРµСЃР°Рј, РґР»РёС‚РµР»СЊРЅРѕСЃС‚Рё Рё С‚РµРєСѓС‰РµРјСѓ РєРѕРЅС‚РµРєСЃС‚Сѓ РїРѕРµР·РґРєРё.</p>
        <Button onClick={() => navigate(appRoutes.routes)}>РЎРјРѕС‚СЂРµС‚СЊ РјР°СЂС€СЂСѓС‚С‹</Button>
      </div>
    </section>
  );
}


