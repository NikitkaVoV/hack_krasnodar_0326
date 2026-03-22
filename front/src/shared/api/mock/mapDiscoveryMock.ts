import type { FilterState, MapEntity, MapSearchRequest, MapSearchResponse } from '@/entities/map/model';

const MOCK_ITEMS: MapEntity[] = [
  {
    id: 'p-1',
    type: 'place',
    title: 'Лавка локальной кухни',
    description: 'Авторская гастро-точка с сезонным меню и локальными продуктами.',
    lat: 45.0355,
    lng: 38.9753,
    distanceKm: 0,
    category: 'gastronomy',
    tags: ['gastronomy', 'popular'],
    imageUrl: 'https://images.unsplash.com/photo-1559339352-11d035aa65de?auto=format&fit=crop&w=1200&q=80',
    popularity: 96,
    isOpenNow: true,
    isFavorite: false,
    badges: ['popular', 'open-now'],
    practicalInfo: 'Средний чек: 1500 ₽, есть летняя веранда.',
    avgBudget: 1500,
    avgDurationMinutes: 90,
  },
  {
    id: 'p-2',
    type: 'place',
    title: 'Панорамный эко-маршрут',
    description: 'Тихая природная тропа вдоль воды с обзорными точками.',
    lat: 45.046,
    lng: 39.020,
    distanceKm: 0,
    category: 'nature',
    tags: ['nature', 'calm', 'family-friendly'],
    imageUrl: 'https://images.unsplash.com/photo-1501785888041-af3ef285b470?auto=format&fit=crop&w=1200&q=80',
    popularity: 82,
    isOpenNow: true,
    isFavorite: false,
    badges: ['new', 'open-now'],
    practicalInfo: 'Подходит для прогулок с детьми и старшим поколением.',
    avgDurationMinutes: 150,
  },
  {
    id: 'e-1',
    type: 'event',
    title: 'Ночной маркет у реки',
    description: 'Музыка, локальные бренды, гастро-корнеры и вечерняя атмосфера.',
    lat: 45.020,
    lng: 38.962,
    distanceKm: 0,
    category: 'events-now',
    tags: ['events-now', 'popular', 'groups'],
    imageUrl: 'https://images.unsplash.com/photo-1528605248644-14dd04022da1?auto=format&fit=crop&w=1200&q=80',
    popularity: 91,
    isOpenNow: true,
    isFavorite: false,
    badges: ['event-ongoing', 'popular'],
    eventStartAt: '2026-03-21T16:00:00+03:00',
    eventEndAt: '2026-03-21T23:30:00+03:00',
  },
  {
    id: 'p-3',
    type: 'place',
    title: 'Скрытый дворик ремесленников',
    description: 'Камерное место вне туристических потоков с мастерскими и арт-кафе.',
    lat: 45.032,
    lng: 38.945,
    distanceKm: 0,
    category: 'hidden-gems',
    tags: ['hidden-gems', 'calm', 'solo'],
    imageUrl: 'https://images.unsplash.com/photo-1473186505569-9c61870c11f9?auto=format&fit=crop&w=1200&q=80',
    popularity: 70,
    isOpenNow: false,
    isFavorite: false,
    badges: ['hidden-gem'],
    practicalInfo: 'Лучшее время посещения: будни до 18:00.',
    avgBudget: 900,
    avgDurationMinutes: 80,
  },
  {
    id: 'e-2',
    type: 'event',
    title: 'Утренний вело-тур по набережным',
    description: 'Активная прогулка с гидом и короткими остановками в ключевых точках.',
    lat: 45.060,
    lng: 39.010,
    distanceKm: 0,
    category: 'active-leisure',
    tags: ['active-leisure', 'groups', 'new'],
    imageUrl: 'https://images.unsplash.com/photo-1461896836934-ffe607ba8211?auto=format&fit=crop&w=1200&q=80',
    popularity: 84,
    isOpenNow: false,
    isFavorite: false,
    badges: ['new'],
    eventStartAt: '2026-03-22T09:00:00+03:00',
    eventEndAt: '2026-03-22T12:00:00+03:00',
  },
  {
    id: 'p-4',
    type: 'place',
    title: 'Семейный городской парк',
    description: 'Просторный парк с детскими зонами и спокойными маршрутами.',
    lat: 45.012,
    lng: 39.040,
    distanceKm: 0,
    category: 'family-friendly',
    tags: ['family-friendly', 'nature', 'elderly'],
    imageUrl: 'https://images.unsplash.com/photo-1472396961693-142e6e269027?auto=format&fit=crop&w=1200&q=80',
    popularity: 88,
    isOpenNow: true,
    isFavorite: false,
    badges: ['popular', 'open-now'],
    practicalInfo: 'Есть прокат, кафе и зоны отдыха.',
    avgDurationMinutes: 120,
  },
  {
    id: 'e-3',
    type: 'event',
    title: 'Камерный джаз в историческом зале',
    description: 'Вечерний концерт для спокойного отдыха и атмосферного завершения дня.',
    lat: 45.025,
    lng: 38.925,
    distanceKm: 0,
    category: 'calm-leisure',
    tags: ['calm-leisure', 'hidden-gems', 'solo'],
    imageUrl: 'https://images.unsplash.com/photo-1511192336575-5a79af67a629?auto=format&fit=crop&w=1200&q=80',
    popularity: 77,
    isOpenNow: false,
    isFavorite: false,
    badges: ['hidden-gem'],
    eventStartAt: '2026-03-22T19:30:00+03:00',
    eventEndAt: '2026-03-22T22:00:00+03:00',
  },
];

function toRad(value: number): number {
  return (value * Math.PI) / 180;
}

function distanceKm(aLat: number, aLng: number, bLat: number, bLng: number): number {
  const earthRadius = 6371;
  const dLat = toRad(bLat - aLat);
  const dLng = toRad(bLng - aLng);
  const x =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRad(aLat)) * Math.cos(toRad(bLat)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
  return 2 * earthRadius * Math.atan2(Math.sqrt(x), Math.sqrt(1 - x));
}

function passesQuickFilters(item: MapEntity, quickFilters: string[]): boolean {
  if (!quickFilters.length) {
    return true;
  }

  return quickFilters.some((filter) => item.tags.includes(filter) || item.category === filter);
}

function passesEntityType(item: MapEntity, filter: FilterState['entityType']): boolean {
  if (filter === 'both') {
    return true;
  }
  if (filter === 'places') {
    return item.type === 'place';
  }
  return item.type === 'event';
}

function passesSuitableFor(item: MapEntity, suitableFor: FilterState['suitableFor']): boolean {
  if (!suitableFor.length) {
    return true;
  }
  return suitableFor.some((tag) => item.tags.includes(tag));
}

function sortItems(items: MapEntity[], sortBy: FilterState['sortBy']): MapEntity[] {
  const sorted = [...items];

  if (sortBy === 'distance') {
    sorted.sort((a, b) => a.distanceKm - b.distanceKm);
    return sorted;
  }

  if (sortBy === 'popularity') {
    sorted.sort((a, b) => b.popularity - a.popularity);
    return sorted;
  }

  if (sortBy === 'starting-soon') {
    sorted.sort((a, b) => {
      const aStart = a.type === 'event' ? Date.parse(a.eventStartAt) : Number.MAX_SAFE_INTEGER;
      const bStart = b.type === 'event' ? Date.parse(b.eventStartAt) : Number.MAX_SAFE_INTEGER;
      return aStart - bStart;
    });
    return sorted;
  }

  sorted.sort((a, b) => {
    const aScore = a.popularity - a.distanceKm * 2;
    const bScore = b.popularity - b.distanceKm * 2;
    return bScore - aScore;
  });
  return sorted;
}

export async function searchMapEntitiesMock(request: MapSearchRequest): Promise<MapSearchResponse> {
  const { lat, lng, radiusKm, filter } = request;

  const filtered = MOCK_ITEMS.map((item) => {
    const dist = distanceKm(lat, lng, item.lat, item.lng);
    return {
      ...item,
      distanceKm: Number(dist.toFixed(2)),
    };
  })
    .filter((item) => item.distanceKm <= radiusKm)
    .filter((item) => passesEntityType(item, filter.entityType))
    .filter((item) => passesQuickFilters(item, filter.quickFilters))
    .filter((item) => (!filter.openNow ? true : item.isOpenNow))
    .filter((item) => passesSuitableFor(item, filter.suitableFor))
    .filter((item) => {
      if (typeof filter.minBudget !== 'number' && typeof filter.maxBudget !== 'number') {
        return true;
      }
      if (item.type !== 'place') {
        return true;
      }
      if (typeof item.avgBudget !== 'number') {
        return false;
      }
      if (typeof filter.minBudget === 'number' && item.avgBudget < filter.minBudget) {
        return false;
      }
      if (typeof filter.maxBudget === 'number' && item.avgBudget > filter.maxBudget) {
        return false;
      }
      return true;
    });

  const sorted = sortItems(filtered, filter.sortBy);

  await new Promise((resolve) => setTimeout(resolve, 350));

  return {
    items: sorted,
    total: sorted.length,
  };
}

