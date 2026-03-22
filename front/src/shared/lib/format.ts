export function formatDate(date: string): string {
  const parsed = new Date(date);
  if (Number.isNaN(parsed.getTime())) {
    return date;
  }
  return new Intl.DateTimeFormat('ru-RU', { dateStyle: 'medium' }).format(parsed);
}

export function formatMinutes(totalMinutes: number): string {
  if (!Number.isFinite(totalMinutes) || totalMinutes < 0) {
    return '-';
  }

  const hours = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;

  if (!hours) {
    return `${minutes} мин`;
  }

  if (!minutes) {
    return `${hours} ч`;
  }

  return `${hours} ч ${minutes} мин`;
}

export function formatBudget(min?: number | null, max?: number | null): string {
  if (typeof min === 'number' && typeof max === 'number') {
    return `${min.toLocaleString('ru-RU')} - ${max.toLocaleString('ru-RU')} ₽`;
  }

  return 'Не указано';
}
