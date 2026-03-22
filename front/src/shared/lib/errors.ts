import axios from 'axios';

export function getApiErrorMessage(error: unknown, fallback = 'РџСЂРѕРёР·РѕС€Р»Р° РѕС€РёР±РєР°'): string {
  if (axios.isAxiosError(error)) {
    const message = error.response?.data?.message;
    if (typeof message === 'string' && message.trim()) {
      return message;
    }
    if (error.response?.status === 401) {
      return 'РЎРµСЃСЃРёСЏ РёСЃС‚РµРєР»Р°. РџРѕР¶Р°Р»СѓР№СЃС‚Р°, РІРѕР№РґРёС‚Рµ СЃРЅРѕРІР°.';
    }
    if (error.message) {
      return error.message;
    }
  }

  if (error instanceof Error && error.message) {
    return error.message;
  }

  return fallback;
}


