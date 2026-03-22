import type { AppRoute } from '@/entities/route/model';
import { SectionTitle } from '@/shared/ui/SectionTitle/SectionTitle';
import { RouteList } from '@/widgets/RouteList/RouteList';
import { EmptyState } from '@/shared/ui/EmptyState/EmptyState';

interface RecommendedRoutesProps {
  routes: AppRoute[];
  isAuthenticated: boolean;
}

export function RecommendedRoutes({ routes, isAuthenticated }: RecommendedRoutesProps) {
  if (!isAuthenticated) {
    return (
      <EmptyState
        title="РџРµСЂСЃРѕРЅР°Р»СЊРЅС‹Рµ СЂРµРєРѕРјРµРЅРґР°С†РёРё"
        description="Р’РѕР№РґРёС‚Рµ РІ Р°РєРєР°СѓРЅС‚, С‡С‚РѕР±С‹ СѓРІРёРґРµС‚СЊ РјР°СЂС€СЂСѓС‚С‹ СЃ СѓС‡С‘С‚РѕРј РІР°С€РёС… РїСЂРµРґРїРѕС‡С‚РµРЅРёР№."
      />
    );
  }

  if (!routes.length) {
    return (
      <EmptyState
        title="Р РµРєРѕРјРµРЅРґР°С†РёР№ РїРѕРєР° РЅРµС‚"
        description="РљР°Рє С‚РѕР»СЊРєРѕ РїРѕСЏРІСЏС‚СЃСЏ РїРѕРґС…РѕРґСЏС‰РёРµ РјР°СЂС€СЂСѓС‚С‹, РѕРЅРё РѕС‚РѕР±СЂР°Р·СЏС‚СЃСЏ Р·РґРµСЃСЊ."
      />
    );
  }

  return (
    <section>
      <SectionTitle title="Р РµРєРѕРјРµРЅРґСѓРµРј РґР»СЏ РІР°СЃ" subtitle="РџРѕРґР±РѕСЂРєР° РЅР° РѕСЃРЅРѕРІРµ РґРѕСЃС‚СѓРїРЅС‹С… РґР°РЅРЅС‹С… РїСЂРѕС„РёР»СЏ" />
      <RouteList routes={routes.slice(0, 3)} />
    </section>
  );
}


