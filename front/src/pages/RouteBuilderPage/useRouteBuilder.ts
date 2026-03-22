import { useEffect, useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import type { MapEntity } from '@/entities/map/model';
import type {
  BuilderPacePreset,
  BuilderTransportMode,
  RouteBuilderRecommendation,
  RouteBuilderRecommendationGroup,
  RouteBuilderSettings,
  RouteBuilderStep,
  RouteBuilderSummary,
  RouteBuilderUserContext,
  RouteBuilderWarning,
} from '@/entities/routeBuilder/model';
import { useDebouncedValue } from '@/shared/hooks/useDebouncedValue';
import { routeBuilderApi } from '@/shared/api/routeBuilderApi';

const KRASNODAR_CENTER = { lat: 45.03547, lng: 38.97531 };

interface DraftStep extends RouteBuilderRecommendation {
  isPinned?: boolean;
}

interface UseRouteBuilderParams {
  userId: string | undefined;
}

function todayIso(): string {
  return new Date().toISOString().slice(0, 10);
}

function parseMinutes(time: string): number {
  const [hoursRaw = '0', minutesRaw = '0'] = time.split(':');
  const hours = Number(hoursRaw);
  const minutes = Number(minutesRaw);
  if (!Number.isFinite(hours) || !Number.isFinite(minutes)) {
    return 10 * 60;
  }
  return Math.max(0, Math.min(23, hours)) * 60 + Math.max(0, Math.min(59, minutes));
}

function formatHHMM(totalMinutes: number): string {
  const normalized = ((totalMinutes % (24 * 60)) + 24 * 60) % (24 * 60);
  const hours = Math.floor(normalized / 60)
    .toString()
    .padStart(2, '0');
  const minutes = (normalized % 60).toString().padStart(2, '0');
  return `${hours}:${minutes}`;
}

function combineDateTime(date: string, time: string): string {
  return `${date}T${time}:00+03:00`;
}

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

function transportSpeedKmH(mode: BuilderTransportMode): number {
  if (mode === 'walk') return 4.5;
  if (mode === 'car') return 28;
  return 14;
}

function estimateVisitMinutes(entity: MapEntity, pace: BuilderPacePreset): number {
  const base =
    entity.type === 'event'
      ? 110
      : typeof entity.avgDurationMinutes === 'number'
        ? entity.avgDurationMinutes
        : 90;

  if (pace === 'active') {
    return Math.max(35, Math.round(base * 0.8));
  }

  if (pace === 'calm' || pace === 'family') {
    return Math.round(base * 1.15);
  }

  if (pace === 'gastro' && entity.tags.includes('gastronomy')) {
    return Math.round(base * 1.2);
  }

  return base;
}

function calcTravelMinutes(
  from: { lat: number; lng: number },
  to: { lat: number; lng: number },
  transportMode: BuilderTransportMode,
): number {
  const directDistance = distanceKm(from.lat, from.lng, to.lat, to.lng);
  const routeDistance = directDistance * 1.18;
  const speed = transportSpeedKmH(transportMode);
  return Math.max(4, Math.round((routeDistance / speed) * 60));
}

function recommendationStatusLabel(item: MapEntity): string {
  if (item.type === 'event') {
    if (!item.eventStartAt) {
      return 'Событие по расписанию';
    }
    const started = Date.parse(item.eventStartAt) <= Date.now();
    return started ? 'Событие уже идет' : 'Скоро начнется';
  }

  return item.isOpenNow ? 'Открыто сейчас' : 'Режим работы уточняется';
}

function mapEntityToRecommendation(
  item: MapEntity,
  kind: RouteBuilderRecommendation['kind'],
  travelMinutesFromLast: number,
  pacePreset: BuilderPacePreset,
): RouteBuilderRecommendation {
  return {
    id: `${kind}-${item.id}`,
    sourceId: item.id,
    kind,
    type: item.type,
    title: item.title,
    description: item.description,
    lat: item.lat,
    lng: item.lng,
    imageUrl: item.imageUrl,
    category: item.category,
    tags: item.tags,
    estimatedVisitMinutes: estimateVisitMinutes(item, pacePreset),
    travelMinutesFromLast,
    distanceKm: item.distanceKm,
    popularity: item.popularity,
    isOpenNow: item.isOpenNow,
    eventStartAt: item.type === 'event' ? item.eventStartAt : undefined,
    eventEndAt: item.type === 'event' ? item.eventEndAt : undefined,
    statusLabel: recommendationStatusLabel(item),
  };
}

function scoreRecommendation(item: MapEntity, userContext: RouteBuilderUserContext | undefined): number {
  const interestScore = item.tags.reduce((acc, tag) => (userContext?.interestTags.includes(tag) ? acc + 2 : acc), 0);
  const preferenceScore = item.tags.reduce((acc, tag) => (userContext?.preferenceTags.includes(tag) ? acc + 1.5 : acc), 0);
  const constraintPenalty = item.tags.reduce((acc, tag) => (userContext?.constraintTags.includes(tag) ? acc + 2 : acc), 0);

  return item.popularity * 0.35 + interestScore + preferenceScore - item.distanceKm * 2.2 - constraintPenalty;
}

function prioritizeByTimeOfDay(items: RouteBuilderRecommendation[], currentHour: number): RouteBuilderRecommendation[] {
  const morning = currentHour >= 6 && currentHour < 11;
  const daytime = currentHour >= 11 && currentHour < 17;
  const evening = currentHour >= 17 && currentHour < 23;

  const score = (item: RouteBuilderRecommendation): number => {
    let value = item.popularity;

    if (morning && (item.tags.includes('nature') || item.tags.includes('gastronomy'))) {
      value += 12;
    }

    if (daytime && (item.tags.includes('culture') || item.tags.includes('family-friendly'))) {
      value += 12;
    }

    if (evening && (item.type === 'event' || item.tags.includes('calm-leisure') || item.tags.includes('gastronomy'))) {
      value += 14;
    }

    return value;
  };

  return [...items].sort((a, b) => score(b) - score(a));
}

function applyTimeline(
  draftSteps: DraftStep[],
  settings: RouteBuilderSettings,
): { steps: RouteBuilderStep[]; summary: RouteBuilderSummary } {
  const plannedStartMinutes = parseMinutes(settings.startTime);
  let cursor = plannedStartMinutes;

  let totalVisit = 0;
  let totalTravel = 0;
  let totalWait = 0;

  const steps = draftSteps.map((step, index) => {
    const travelMinutes = index === 0 ? 0 : step.travelMinutesFromLast;
    cursor += travelMinutes;

    let waitMinutes = 0;
    if (step.type === 'event' && step.eventStartAt) {
      const eventDate = new Date(step.eventStartAt);
      const eventMinutes = eventDate.getHours() * 60 + eventDate.getMinutes();
      if (cursor < eventMinutes) {
        waitMinutes = eventMinutes - cursor;
        cursor = eventMinutes;
      }
    }

    const startMinutes = cursor;
    cursor += step.estimatedVisitMinutes;

    const endMinutes = cursor;
    totalVisit += step.estimatedVisitMinutes;
    totalTravel += travelMinutes;
    totalWait += waitMinutes;

    return {
      ...step,
      order: index + 1,
      travelMinutesFromLast: travelMinutes,
      waitMinutes,
      plannedStartAt: combineDateTime(settings.date, formatHHMM(startMinutes)),
      plannedEndAt: combineDateTime(settings.date, formatHHMM(endMinutes)),
    } satisfies RouteBuilderStep;
  });

  const totalPlanned = totalVisit + totalTravel + totalWait;
  const remaining = settings.totalDurationMinutes - totalPlanned;

  return {
    steps,
    summary: {
      stepsCount: steps.length,
      totalVisitMinutes: totalVisit,
      totalTravelMinutes: totalTravel,
      totalWaitMinutes: totalWait,
      totalPlannedMinutes: totalPlanned,
      remainingMinutes: remaining,
      status: remaining < 0 ? 'overloaded' : remaining <= 45 ? 'tight' : 'ok',
    },
  };
}

function createWarnings(
  steps: RouteBuilderStep[],
  summary: RouteBuilderSummary,
  settings: RouteBuilderSettings,
  userContext: RouteBuilderUserContext | undefined,
): RouteBuilderWarning[] {
  const warnings: RouteBuilderWarning[] = [];

  if (summary.remainingMinutes < 0) {
    warnings.push({
      id: 'duration-overflow',
      level: 'critical',
      title: 'Маршрут не помещается во время',
      message: `Превышение лимита примерно на ${Math.abs(summary.remainingMinutes)} мин.`,
    });
  }

  const travelShare = summary.totalPlannedMinutes > 0 ? summary.totalTravelMinutes / summary.totalPlannedMinutes : 0;
  if (travelShare > 0.35) {
    warnings.push({
      id: 'too-much-travel',
      level: 'warning',
      title: 'Слишком много времени в дороге',
      message: 'Рассмотрите более близкие точки, чтобы маршрут был комфортнее.',
    });
  }

  steps.forEach((step) => {
    if (settings.onlyOpenPlaces && step.type === 'place' && !step.isOpenNow) {
      warnings.push({
        id: `closed-${step.sourceId}`,
        level: 'warning',
        title: 'Место может быть закрыто',
        message: `Проверьте режим работы для шага ${step.order}.`,
        stepOrder: step.order,
      });
    }

    if (step.type === 'event' && step.eventStartAt) {
      const eventStart = Date.parse(step.eventStartAt);
      const arrival = Date.parse(step.plannedStartAt);
      if (arrival > eventStart + 45 * 60 * 1000) {
        warnings.push({
          id: `event-miss-${step.sourceId}`,
          level: 'critical',
          title: 'Можно опоздать на событие',
          message: `Шаг ${step.order} начинается позже старта события.`,
          stepOrder: step.order,
        });
      }
    }

    if (userContext?.constraintTags.length) {
      const conflict = step.tags.find((tag) => userContext.constraintTags.includes(tag));
      if (conflict) {
        warnings.push({
          id: `constraint-${step.sourceId}`,
          level: 'warning',
          title: 'Проверьте персональные ограничения',
          message: `Шаг ${step.order} пересекается с ограничением: ${conflict}.`,
          stepOrder: step.order,
        });
      }
    }
  });

  if (steps.length >= 6 && summary.remainingMinutes < 60) {
    warnings.push({
      id: 'overloaded',
      level: 'warning',
      title: 'Маршрут выглядит перегруженным',
      message: 'Можно сократить 1-2 шага, чтобы оставить больше времени на отдых.',
    });
  }

  return warnings;
}

function getCurrentHour(time: string): number {
  const [hoursRaw = '10'] = time.split(':');
  const value = Number(hoursRaw);
  if (!Number.isFinite(value)) {
    return 10;
  }
  return Math.max(0, Math.min(23, value));
}

export function useRouteBuilder({ userId }: UseRouteBuilderParams) {
  const [settings, setSettings] = useState<RouteBuilderSettings>({
    date: todayIso(),
    startTime: '10:00',
    totalDurationMinutes: 420,
    radiusKm: 12,
    transportMode: 'mixed',
    pacePreset: 'mixed',
    onlyOpenPlaces: false,
    includeEvents: true,
    manualSearch: '',
  });
  const [anchorPoint, setAnchorPoint] = useState<{ lat: number; lng: number }>(KRASNODAR_CENTER);
  const [draftSteps, setDraftSteps] = useState<DraftStep[]>([]);
  const [selectedStepId, setSelectedStepId] = useState<string | null>(null);
  const [replacingStepSourceId, setReplacingStepSourceId] = useState<string | null>(null);

  const userContextQuery = useQuery({
    queryKey: userId ? ['builder', 'user-context', userId] : ['builder', 'user-context', 'guest'],
    enabled: Boolean(userId),
    queryFn: () => routeBuilderApi.getUserContext(userId!),
    staleTime: 2 * 60 * 1000,
  });

  useEffect(() => {
    const location = userContextQuery.data?.lastLocation;
    if (!location || draftSteps.length) {
      return;
    }
    setAnchorPoint((prev) => {
      if (Math.abs(prev.lat - KRASNODAR_CENTER.lat) > 0.0001 || Math.abs(prev.lng - KRASNODAR_CENTER.lng) > 0.0001) {
        return prev;
      }
      return location;
    });
  }, [draftSteps.length, userContextQuery.data?.lastLocation]);

  const searchPoint = useMemo(() => {
    const lastStep = draftSteps[draftSteps.length - 1];
    if (!lastStep) {
      return anchorPoint;
    }
    return { lat: lastStep.lat, lng: lastStep.lng };
  }, [anchorPoint, draftSteps]);

  const debouncedSearchParams = useDebouncedValue(
    {
      lat: searchPoint.lat,
      lng: searchPoint.lng,
      settings,
    },
    280,
  );

  const candidatesQuery = useQuery({
    queryKey: ['builder', 'candidates', debouncedSearchParams],
    queryFn: () => routeBuilderApi.getCandidates(debouncedSearchParams),
  });

  const recommendations = useMemo(() => {
    const items = candidatesQuery.data ?? [];
    const userContext = userContextQuery.data;
    const selectedSourceIds = new Set(draftSteps.map((step) => step.sourceId));

    const base = items
      .filter((item) => !selectedSourceIds.has(item.id))
      .sort((a, b) => scoreRecommendation(b, userContext) - scoreRecommendation(a, userContext));

    const last = draftSteps[draftSteps.length - 1] ?? null;

    const withTravel = (item: MapEntity, kind: RouteBuilderRecommendation['kind']) => {
      const from = last ? { lat: last.lat, lng: last.lng } : searchPoint;
      const travelMinutesFromLast = calcTravelMinutes(from, { lat: item.lat, lng: item.lng }, settings.transportMode);
      return mapEntityToRecommendation(item, kind, travelMinutesFromLast, settings.pacePreset);
    };

    const startPool = base.filter((item) => item.type === 'place' || item.isOpenNow).slice(0, 16).map((item) => withTravel(item, 'start'));

    const nextPool = base
      .filter((item) => {
        if (!last) {
          return false;
        }
        const dist = distanceKm(last.lat, last.lng, item.lat, item.lng);
        return dist <= Math.max(1.2, settings.radiusKm * 0.42);
      })
      .slice(0, 18)
      .map((item) => withTravel(item, 'next'));

    const eventsPool = base
      .filter((item) => item.type === 'event')
      .map((item) => withTravel(item, 'events'))
      .filter((item) => {
        if (!item.eventStartAt) {
          return true;
        }

        const expectedArrivalMinutes =
          parseMinutes(settings.startTime) +
          draftSteps.reduce((acc, step) => acc + step.estimatedVisitMinutes + step.travelMinutesFromLast, 0) +
          item.travelMinutesFromLast;

        const eventStart = new Date(item.eventStartAt);
        const eventStartMinutes = eventStart.getHours() * 60 + eventStart.getMinutes();
        return expectedArrivalMinutes <= eventStartMinutes + 60;
      })
      .slice(0, 12);

    const alternativePool = base
      .map((item) => withTravel(item, 'alternative'))
      .sort((a, b) => (a.travelMinutesFromLast + a.estimatedVisitMinutes) - (b.travelMinutesFromLast + b.estimatedVisitMinutes))
      .slice(0, 10);

    const currentHour = getCurrentHour(settings.startTime);

    return {
      start: prioritizeByTimeOfDay(startPool, currentHour).slice(0, 6),
      next: prioritizeByTimeOfDay(nextPool, currentHour).slice(0, 6),
      events: prioritizeByTimeOfDay(eventsPool, currentHour).slice(0, 6),
      alternative: alternativePool,
    };
  }, [
    candidatesQuery.data,
    draftSteps,
    searchPoint,
    settings.pacePreset,
    settings.radiusKm,
    settings.startTime,
    settings.transportMode,
    userContextQuery.data,
  ]);

  const timeline = useMemo(() => applyTimeline(draftSteps, settings), [draftSteps, settings]);

  const warnings = useMemo(
    () => createWarnings(timeline.steps, timeline.summary, settings, userContextQuery.data),
    [timeline.steps, timeline.summary, settings, userContextQuery.data],
  );

  const recommendationGroups = useMemo<RouteBuilderRecommendationGroup[]>(() => {
    const hasSteps = timeline.steps.length > 0;

    return [
      {
        id: 'start',
        title: 'Рекомендуем начать',
        subtitle: 'Персональный старт маршрута по вашим интересам и параметрам дня.',
        items: hasSteps ? [] : recommendations.start,
        emptyTitle: 'Добавьте первую точку',
        emptyDescription: hasSteps
          ? 'Старт уже выбран. Теперь смотрите рекомендации следующего шага.'
          : 'Смените радиус или ослабьте ограничения, чтобы увидеть больше вариантов.',
      },
      {
        id: 'next',
        title: 'Рядом и логично дальше',
        subtitle: 'Точки поблизости от последнего шага с минимальными переездами.',
        items: hasSteps ? recommendations.next : [],
        emptyTitle: hasSteps ? 'Нет близких продолжений' : 'Добавьте первую точку маршрута',
        emptyDescription: hasSteps
          ? 'Попробуйте увеличить радиус или выбрать другой последний шаг.'
          : 'После первого шага здесь появятся контекстные варианты продолжения.',
      },
      {
        id: 'events',
        title: 'События, в которые вы успеваете',
        subtitle: 'Подборка событий с учетом текущего окна времени маршрута.',
        items: settings.includeEvents ? recommendations.events : [],
        emptyTitle: settings.includeEvents ? 'Нет подходящих событий' : 'События отключены в настройках',
        emptyDescription: settings.includeEvents
          ? 'Сдвиньте время старта или увеличьте длительность маршрута.'
          : 'Включите тумблер «Добавлять события», чтобы получить эту подборку.',
      },
      {
        id: 'alternative',
        title: 'Более спокойная альтернатива',
        subtitle: 'Короткие и менее нагруженные варианты для гибкой перестройки маршрута.',
        items: recommendations.alternative,
        emptyTitle: 'Пока нет альтернатив',
        emptyDescription: 'Добавьте шаги или расширьте поиск, чтобы появились запасные варианты.',
      },
    ];
  }, [recommendations, settings.includeEvents, timeline.steps.length]);

  const addStepFromRecommendation = (recommendation: RouteBuilderRecommendation) => {
    setDraftSteps((prev) => {
      const next: DraftStep[] = [...prev];

      if (replacingStepSourceId) {
        const index = next.findIndex((step) => step.sourceId === replacingStepSourceId);
        if (index >= 0) {
          next[index] = { ...recommendation, isPinned: next[index].isPinned };
          return next;
        }
      }

      return [...next, recommendation];
    });

    setReplacingStepSourceId(null);
    setSelectedStepId(recommendation.sourceId);
  };

  const removeStep = (sourceId: string) => {
    setDraftSteps((prev) => prev.filter((step) => step.sourceId !== sourceId));
    setReplacingStepSourceId((prev) => (prev === sourceId ? null : prev));
    setSelectedStepId((prev) => (prev === sourceId ? null : prev));
  };

  const moveStep = (sourceId: string, direction: 'up' | 'down') => {
    setDraftSteps((prev) => {
      const index = prev.findIndex((step) => step.sourceId === sourceId);
      if (index < 0) {
        return prev;
      }

      const offset = direction === 'up' ? -1 : 1;
      const targetIndex = index + offset;

      if (targetIndex < 0 || targetIndex >= prev.length) {
        return prev;
      }

      const next = [...prev];
      const [item] = next.splice(index, 1);
      next.splice(targetIndex, 0, item);
      return next;
    });
  };

  const togglePin = (sourceId: string) => {
    setDraftSteps((prev) => prev.map((step) => (step.sourceId === sourceId ? { ...step, isPinned: !step.isPinned } : step)));
  };

  const clearAll = () => {
    setDraftSteps([]);
    setSelectedStepId(null);
    setReplacingStepSourceId(null);
  };

  const selectedStep = useMemo(
    () => timeline.steps.find((step) => step.sourceId === selectedStepId) ?? null,
    [selectedStepId, timeline.steps],
  );

  return {
    settings,
    setSettings,
    anchorPoint,
    setAnchorPoint,
    timeline,
    warnings,
    recommendationGroups,
    selectedStep,
    selectedStepId,
    setSelectedStepId,
    replacingStepSourceId,
    setReplacingStepSourceId,
    userContext: userContextQuery.data,
    isUserContextLoading: userContextQuery.isLoading,
    candidatesQuery,
    addStepFromRecommendation,
    removeStep,
    moveStep,
    togglePin,
    clearAll,
  };
}

