import type { MapEntity } from '@/entities/map/model';

const CATEGORY_LABELS: Record<string, string> = {
  gastronomy: 'Гастрономия',
  nature: 'Природа',
  'active-leisure': 'Активный отдых',
  'calm-leisure': 'Спокойный отдых',
  'events-now': 'События сейчас',
  'family-friendly': 'Для семьи',
  'hidden-gems': 'Нетуристические места',
  popular: 'Популярное',
};

export function badgeLabel(badge: MapEntity['badges'][number]): string {
  if (badge === 'popular') return 'Популярно';
  if (badge === 'new') return 'Новое';
  if (badge === 'hidden-gem') return 'Нетуристично';
  if (badge === 'open-now') return 'Открыто';
  return 'Идёт сейчас';
}

export function categoryLabel(category: string): string {
  const key = category.trim().toLowerCase();
  return CATEGORY_LABELS[key] ?? category;
}

export function openStatusLabel(isOpenNow: boolean): string {
  return isOpenNow ? 'Открыто сейчас' : 'Сейчас закрыто';
}

export function localizePracticalInfo(value: string): string {
  return value
    .replace(/address\s*:/gi, 'Адрес:')
    .replace(/open\s*now/gi, 'Открыто сейчас')
    .replace(/closed/gi, 'Закрыто')
    .replace(/open/gi, 'Открыто');
}
