export const env = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL ?? '',
  yandexMapsApiKey: import.meta.env.VITE_YANDEX_MAPS_API_KEY ?? '',
  enableMapMockFallback: String(import.meta.env.VITE_ENABLE_MAP_MOCK ?? 'false').toLowerCase() === 'true',
};
