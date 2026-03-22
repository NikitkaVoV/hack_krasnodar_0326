import type { PlaceDto } from './dto';
import type { PlaceCardModel } from './model';

export function mapPlaceDtoToPlaceCardModel(dto: PlaceDto): PlaceCardModel {
  return {
    id: dto.id,
    name: dto.name ?? 'Р›РѕРєР°С†РёСЏ',
    description: dto.description ?? 'РћРїРёСЃР°РЅРёРµ РїРѕРєР° РЅРµРґРѕСЃС‚СѓРїРЅРѕ',
    location: dto.location ?? 'Р›РѕРєР°С†РёСЏ РЅРµ СѓРєР°Р·Р°РЅР°',
  };
}


