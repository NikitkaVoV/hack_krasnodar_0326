import { jsPDF } from 'jspdf';
import robotoRegularUrl from '@/assets/fonts/Roboto-Regular.ttf';
import { formatMinutes } from '@/shared/lib/format';
import type { RoutePdfModel, RoutePdfStepModel } from './types';

const PAGE_MARGIN = 14;
const BLOCK_GAP = 3;

const COLORS = {
  ink: [26, 33, 45] as const,
  muted: [99, 112, 133] as const,
  accent: [100, 78, 142] as const,
  accentSoft: [235, 229, 243] as const,
  panelBg: [246, 248, 252] as const,
  panelBorder: [214, 221, 232] as const,
  banner: [36, 48, 71] as const,
};

let robotoBase64: string | null = null;

function toBase64(arrayBuffer: ArrayBuffer): string {
  let binary = '';
  const bytes = new Uint8Array(arrayBuffer);
  const chunkSize = 0x8000;

  for (let i = 0; i < bytes.length; i += chunkSize) {
    const chunk = bytes.subarray(i, i + chunkSize);
    binary += String.fromCharCode(...chunk);
  }

  return btoa(binary);
}

async function ensureRoboto(doc: jsPDF): Promise<void> {
  if (!robotoBase64) {
    const response = await fetch(robotoRegularUrl);
    const arrayBuffer = await response.arrayBuffer();
    robotoBase64 = toBase64(arrayBuffer);
  }

  doc.addFileToVFS('Roboto-Regular.ttf', robotoBase64);
  doc.addFont('Roboto-Regular.ttf', 'Roboto', 'normal');
  doc.addFont('Roboto-Regular.ttf', 'Roboto', 'bold');
  doc.setFont('Roboto', 'normal');
}

function normalizeText(value?: string): string {
  return value?.trim() || '—';
}

function formatDate(date?: string): string {
  if (!date) {
    return '—';
  }

  const parsed = Date.parse(date);
  if (!Number.isFinite(parsed)) {
    return date;
  }

  return new Date(parsed).toLocaleString('ru-RU', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
}

function formatCoordinates(lat?: number, lng?: number): string {
  if (typeof lat !== 'number' || typeof lng !== 'number') {
    return '—';
  }

  return `${lat.toFixed(5)}, ${lng.toFixed(5)}`;
}

function setColor(doc: jsPDF, rgb: readonly [number, number, number], target: 'text' | 'draw' | 'fill') {
  if (target === 'text') {
    doc.setTextColor(rgb[0], rgb[1], rgb[2]);
    return;
  }

  if (target === 'draw') {
    doc.setDrawColor(rgb[0], rgb[1], rgb[2]);
    return;
  }

  doc.setFillColor(rgb[0], rgb[1], rgb[2]);
}

function buildStepMetaLines(step: RoutePdfStepModel): string[] {
  const lines = [
    `Тип: ${step.type}`,
    `Адрес: ${normalizeText(step.address)}`,
    `Координаты: ${formatCoordinates(step.lat, step.lng)}`,
    `Длительность: ${step.durationMinutes ? formatMinutes(step.durationMinutes) : '—'}`,
    `Время: ${
      step.plannedStart || step.plannedEnd ? `${step.plannedStart ?? '—'} → ${step.plannedEnd ?? '—'}` : '—'
    }`,
    `Переезд: ${typeof step.travelMinutes === 'number' ? `${step.travelMinutes} мин` : '—'}`,
    `Ожидание: ${typeof step.waitMinutes === 'number' ? `${step.waitMinutes} мин` : '—'}`,
    `Заметки: ${normalizeText(step.notes)}`,
  ];

  if (step.place) {
    lines.push(`Место: ${step.place.name}${step.place.location ? ` (${step.place.location})` : ''}`);
  }

  if (step.event) {
    lines.push(`Событие: ${step.event.name}${step.event.startAt ? `, старт ${formatDate(step.event.startAt)}` : ''}`);
  }

  return lines;
}

function estimateStepHeight(doc: jsPDF, step: RoutePdfStepModel, width: number): number {
  doc.setFont('Roboto', 'bold');
  doc.setFontSize(12);
  const titleLines = doc.splitTextToSize(`${step.order}. ${step.title}`, width - 28);

  doc.setFont('Roboto', 'normal');
  doc.setFontSize(10);

  const descriptionLines = doc.splitTextToSize(`Описание: ${normalizeText(step.description)}`, width - 10);

  const metaLineCount = buildStepMetaLines(step).reduce((acc, line) => {
    const split = doc.splitTextToSize(line, width - 10);
    return acc + split.length;
  }, 0);

  const totalLines = titleLines.length + descriptionLines.length + metaLineCount;

  return 12 + totalLines * 4.2 + 8;
}

export async function generateRoutePdfDocument(model: RoutePdfModel): Promise<Blob> {
  const doc = new jsPDF({
    orientation: 'portrait',
    unit: 'mm',
    format: 'a4',
  });

  await ensureRoboto(doc);

  const pageWidth = doc.internal.pageSize.getWidth();
  const pageHeight = doc.internal.pageSize.getHeight();
  const contentWidth = pageWidth - PAGE_MARGIN * 2;

  let cursorY = PAGE_MARGIN;

  const ensureSpace = (height: number) => {
    if (cursorY + height <= pageHeight - PAGE_MARGIN - 8) {
      return;
    }

    doc.addPage();
    doc.setFont('Roboto', 'normal');
    cursorY = PAGE_MARGIN;
  };

  const writeWrapped = (
    text: string,
    options?: { size?: number; bold?: boolean; color?: readonly [number, number, number]; width?: number; lineGap?: number },
  ): number => {
    const size = options?.size ?? 10;
    const lineGap = options?.lineGap ?? 4.2;
    const width = options?.width ?? contentWidth;

    doc.setFont('Roboto', options?.bold ? 'bold' : 'normal');
    doc.setFontSize(size);
    setColor(doc, options?.color ?? COLORS.ink, 'text');

    const lines = doc.splitTextToSize(text, width);
    const height = lines.length * lineGap;

    ensureSpace(height + 1);
    doc.text(lines, PAGE_MARGIN, cursorY);
    cursorY += height;

    return height;
  };

  const drawBanner = () => {
    const bannerHeight = 32;
    ensureSpace(bannerHeight + 4);

    setColor(doc, COLORS.banner, 'fill');
    doc.roundedRect(PAGE_MARGIN, cursorY, contentWidth, bannerHeight, 3, 3, 'F');

    doc.setFont('Roboto', 'bold');
    doc.setFontSize(18);
    setColor(doc, [248, 250, 252], 'text');
    const titleLines = doc.splitTextToSize(model.title, contentWidth - 8);
    doc.text(titleLines, PAGE_MARGIN + 4, cursorY + 9);

    doc.setFont('Roboto', 'normal');
    doc.setFontSize(9);
    setColor(doc, [210, 219, 234], 'text');

    const subtitle = [
      `ID: ${model.routeId}`,
      model.date ? `Дата: ${model.date}` : null,
      model.durationMinutes ? `Длительность: ${formatMinutes(model.durationMinutes)}` : null,
    ]
      .filter(Boolean)
      .join(' • ');

    doc.text(subtitle, PAGE_MARGIN + 4, cursorY + bannerHeight - 6);
    cursorY += bannerHeight + BLOCK_GAP;
  };

  const drawSectionTitle = (title: string) => {
    ensureSpace(10);

    setColor(doc, COLORS.accentSoft, 'fill');
    doc.roundedRect(PAGE_MARGIN, cursorY, contentWidth, 8, 2, 2, 'F');

    setColor(doc, COLORS.accent, 'fill');
    doc.rect(PAGE_MARGIN, cursorY, 2.5, 8, 'F');

    doc.setFont('Roboto', 'bold');
    doc.setFontSize(12);
    setColor(doc, COLORS.ink, 'text');
    doc.text(title, PAGE_MARGIN + 5, cursorY + 5.5);

    cursorY += 11;
  };

  const drawInfoCard = (lines: string[]) => {
    doc.setFont('Roboto', 'normal');
    doc.setFontSize(10);

    const prepared = lines.map((line) => doc.splitTextToSize(line, contentWidth - 10));
    const textHeight = prepared.reduce((acc, linesSet) => acc + linesSet.length * 4.1, 0);
    const cardHeight = textHeight + 8;

    ensureSpace(cardHeight + 2);

    setColor(doc, COLORS.panelBorder, 'draw');
    setColor(doc, COLORS.panelBg, 'fill');
    doc.roundedRect(PAGE_MARGIN, cursorY, contentWidth, cardHeight, 2, 2, 'FD');

    let innerY = cursorY + 5;
    prepared.forEach((lineSet) => {
      setColor(doc, COLORS.ink, 'text');
      doc.text(lineSet, PAGE_MARGIN + 5, innerY);
      innerY += lineSet.length * 4.1;
    });

    cursorY += cardHeight + 2;
  };

  const drawSummaryPanel = () => {
    const lines = [
      `Описание: ${normalizeText(model.description)}`,
      `Кратко: ${normalizeText(model.summary)}`,
      `Рекомендации: ${normalizeText(model.advice)}`,
      `Автор: ${normalizeText(model.author)}`,
    ];

    drawInfoCard(lines);
  };

  const drawUsefulFields = () => {
    const lines = model.usefulFields.map((item) => `${item.label}: ${normalizeText(item.value)}`);
    drawInfoCard(lines);
  };

  const drawStepCard = (step: RoutePdfStepModel) => {
    const estimatedHeight = estimateStepHeight(doc, step, contentWidth);
    ensureSpace(estimatedHeight + 3);

    const startY = cursorY;

    setColor(doc, COLORS.panelBorder, 'draw');
    setColor(doc, [250, 252, 255], 'fill');
    doc.roundedRect(PAGE_MARGIN, startY, contentWidth, estimatedHeight, 2, 2, 'FD');

    setColor(doc, COLORS.accent, 'fill');
    doc.circle(PAGE_MARGIN + 6, startY + 7, 3.6, 'F');

    doc.setFont('Roboto', 'bold');
    doc.setFontSize(9);
    setColor(doc, [255, 255, 255], 'text');
    doc.text(String(step.order), PAGE_MARGIN + 6, startY + 8.3, { align: 'center' });

    let innerY = startY + 5.8;

    doc.setFont('Roboto', 'bold');
    doc.setFontSize(12);
    setColor(doc, COLORS.ink, 'text');
    const titleLines = doc.splitTextToSize(`${step.order}. ${step.title}`, contentWidth - 28);
    doc.text(titleLines, PAGE_MARGIN + 13, innerY);
    innerY += titleLines.length * 4.5;

    doc.setFont('Roboto', 'normal');
    doc.setFontSize(10);
    const descriptionLines = doc.splitTextToSize(`Описание: ${normalizeText(step.description)}`, contentWidth - 10);
    doc.text(descriptionLines, PAGE_MARGIN + 5, innerY);
    innerY += descriptionLines.length * 4.2;

    buildStepMetaLines(step).forEach((line) => {
      const split = doc.splitTextToSize(line, contentWidth - 10);
      doc.text(split, PAGE_MARGIN + 5, innerY);
      innerY += split.length * 4.2;
    });

    cursorY = startY + Math.max(estimatedHeight, innerY - startY + 2) + 2;
  };

  const drawAlternatives = () => {
    if (!model.alternatives.length) {
      return;
    }

    drawSectionTitle('Альтернативы и дополнительные точки');

    model.alternatives.forEach((item, index) => {
      writeWrapped(`${index + 1}. ${item.title}${item.description ? ` — ${item.description}` : ''}`, {
        size: 10,
        color: COLORS.ink,
      });
      cursorY += 0.5;
    });

    cursorY += 1;
  };

  const drawFinalSummary = () => {
    drawSectionTitle('Итог');

    drawInfoCard([
      `Количество точек: ${model.steps.length}`,
      `Суммарная длительность: ${model.durationMinutes ? formatMinutes(model.durationMinutes) : '—'}`,
      `Дата экспорта: ${formatDate(model.exportedAt)}`,
    ]);
  };

  drawBanner();
  drawSectionTitle('Заголовок маршрута');
  drawSummaryPanel();

  drawSectionTitle('Основная информация');
  drawUsefulFields();

  drawSectionTitle('Точки маршрута');
  if (!model.steps.length) {
    drawInfoCard(['Подробные точки маршрута отсутствуют. Экспорт содержит только общую информацию.']);
  } else {
    model.steps.forEach((step) => drawStepCard(step));
  }

  drawAlternatives();
  drawFinalSummary();

  const totalPages = doc.getNumberOfPages();

  for (let page = 1; page <= totalPages; page += 1) {
    doc.setPage(page);
    doc.setFont('Roboto', 'normal');
    doc.setFontSize(8);
    setColor(doc, COLORS.muted, 'text');
    doc.text(
      `Экспорт маршрута · Страница ${page}/${totalPages}`,
      pageWidth / 2,
      pageHeight - 6,
      { align: 'center' },
    );
  }

  return doc.output('blob');
}
