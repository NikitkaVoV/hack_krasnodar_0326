import { useNavigate } from 'react-router-dom';
import { appRoutes } from '@/shared/const/routes';
import { Button } from '@/shared/ui/Button/Button';
import styles from './HomeFinalCta.module.css';

interface HomeFinalCtaProps {
  isAuthenticated: boolean;
}

export function HomeFinalCta({ isAuthenticated }: HomeFinalCtaProps) {
  const navigate = useNavigate();

  return (
    <section className={styles.cta}>
      <h2>
        {isAuthenticated
          ? 'РџСЂРѕРґРѕР»Р¶Р°Р№С‚Рµ РёСЃСЃР»РµРґРѕРІР°С‚СЊ РЅРѕРІС‹Рµ РјР°СЂС€СЂСѓС‚С‹'
          : 'РќР°С‡РЅРёС‚Рµ СЃ РїРµСЂСЃРѕРЅР°Р»СЊРЅС‹С… СЂРµРєРѕРјРµРЅРґР°С†РёР№'}
      </h2>
      <p>
        {isAuthenticated
          ? 'РћС‚РєСЂРѕР№С‚Рµ СЃРІРµР¶РёРµ РїРѕРґР±РѕСЂРєРё Рё СЃРѕР±РµСЂРёС‚Рµ СЃР»РµРґСѓСЋС‰РёР№ РјР°СЂС€СЂСѓС‚ Р·Р° РїР°СЂСѓ РјРёРЅСѓС‚.'
          : 'Р’РѕР№РґРёС‚Рµ РІ Р°РєРєР°СѓРЅС‚ Рё РїРѕР»СѓС‡РёС‚Рµ РјР°СЂС€СЂСѓС‚С‹ РїРѕРґ СЃРІРѕРё РёРЅС‚РµСЂРµСЃС‹.'}
      </p>
      <div className={styles.actions}>
        {isAuthenticated ? (
          <Button onClick={() => navigate(appRoutes.routeBuilder)}>РџРѕРїСЂРѕР±РѕРІР°С‚СЊ РєРѕРЅСЃС‚СЂСѓРєС‚РѕСЂ</Button>
        ) : (
          <Button onClick={() => navigate(appRoutes.login)}>Р’РѕР№С‚Рё</Button>
        )}
        <Button variant="ghost" onClick={() => navigate(appRoutes.routes)}>
          РЎРјРѕС‚СЂРµС‚СЊ РІСЃРµ РјР°СЂС€СЂСѓС‚С‹
        </Button>
      </div>
    </section>
  );
}


