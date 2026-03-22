import { env } from '@/shared/config/env';

let ymapsLoader: Promise<Window['ymaps']> | null = null;

function buildYMapsUrl(): string {
  const base = 'https://api-maps.yandex.ru/2.1/?lang=ru_RU';
  if (!env.yandexMapsApiKey) {
    return base;
  }
  return `${base}&apikey=${encodeURIComponent(env.yandexMapsApiKey)}`;
}

export function loadYMaps(): Promise<Window['ymaps']> {
  if (typeof window === 'undefined') {
    return Promise.reject(new Error('Yandex Maps доступны только в браузере.'));
  }

  if (window.ymaps) {
    return new Promise((resolve) => {
      window.ymaps?.ready(() => resolve(window.ymaps));
    });
  }

  if (ymapsLoader) {
    return ymapsLoader;
  }

  ymapsLoader = new Promise((resolve, reject) => {
    const script = document.createElement('script');
    script.src = buildYMapsUrl();
    script.async = true;
    script.onload = () => {
      if (!window.ymaps) {
        reject(new Error('Скрипт карты загружен, но ymaps не инициализировался.'));
        return;
      }
      window.ymaps.ready(() => resolve(window.ymaps));
    };
    script.onerror = () => reject(new Error('Не удалось загрузить скрипт Yandex Maps.'));
    document.head.appendChild(script);
  });

  return ymapsLoader;
}
