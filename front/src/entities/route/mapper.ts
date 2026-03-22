import type { RouteDto, RouteStepDto } from './dto';
import type { AppRoute, AppRouteStep } from './model';

function mapTargetType(targetType: string): AppRouteStep['targetType'] {
  const value = targetType.toLowerCase();
  if (value.includes('place')) {
    return 'place';
  }
  if (value.includes('event')) {
    return 'event';
  }
  return 'unknown';
}

export function mapRouteDtoToAppRoute(dto: RouteDto): AppRoute {
  return {
    id: dto.id,
    userId: dto.user,
    date: dto.date,
    totalDurationMinutes: dto.totalDuration,
    summary: dto.summary,
    advice: dto.advice,
  };
}

export function mapRouteStepDtoToAppRouteStep(dto: RouteStepDto): AppRouteStep {
  return {
    targetId: dto.targetId,
    targetType: mapTargetType(dto.targetType),
    stepOrder: dto.stepOrder,
    plannedTimeStart: dto.plannedTimeStart,
    plannedTimeEnd: dto.plannedTimeEnd,
    travelTimeMinutes: dto.travelTimeMinutes,
    waitTimeMinutes: dto.waitTimeMinutes,
    notes: dto.notes,
    transportMode: dto.transportMode,
    priority: dto.priority,
  };
}


