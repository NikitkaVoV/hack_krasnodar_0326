import type { EventDto } from './dto';
import type { EventCardModel } from './model';

export function mapEventDtoToEventCardModel(dto: EventDto): EventCardModel {
  return {
    id: dto.id,
    name: dto.name ?? 'РЎРѕР±С‹С‚РёРµ',
    description: dto.description ?? 'РћРїРёСЃР°РЅРёРµ РїРѕРєР° РЅРµРґРѕСЃС‚СѓРїРЅРѕ',
    location: dto.location ?? 'Р›РѕРєР°С†РёСЏ РЅРµ СѓРєР°Р·Р°РЅР°',
    startAt: dto.startAt,
  };
}


