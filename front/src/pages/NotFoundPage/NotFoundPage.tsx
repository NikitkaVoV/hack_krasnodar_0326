import { Link } from 'react-router-dom';
import { PageContainer } from '@/shared/ui/PageContainer/PageContainer';
import { Card } from '@/shared/ui/Card/Card';
import { appRoutes } from '@/shared/const/routes';
import { Button } from '@/shared/ui/Button/Button';

export function NotFoundPage() {
  return (
    <PageContainer>
      <Card>
        <h1>404: РЎС‚СЂР°РЅРёС†Р° РЅРµ РЅР°Р№РґРµРЅР°</h1>
        <p>РџРѕС…РѕР¶Рµ, Р°РґСЂРµСЃ РЅРµРІРµСЂРЅС‹Р№ РёР»Рё СЃС‚СЂР°РЅРёС†Р° Р±С‹Р»Р° РїРµСЂРµРјРµС‰РµРЅР°.</p>
        <Link to={appRoutes.home}>
          <Button>РќР° РіР»Р°РІРЅСѓСЋ</Button>
        </Link>
      </Card>
    </PageContainer>
  );
}


