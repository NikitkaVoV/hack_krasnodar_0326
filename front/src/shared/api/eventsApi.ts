import { http } from '@/shared/api/http';
import type { EventDto } from '@/entities/event/dto';

export const eventsApi = {
  async getEventById(eventId: string): Promise<EventDto> {
    const { data } = await http.get<EventDto>(`/api/route-steps/events/${eventId}`);
    return data;
  },
  async getUpcomingEventsFallback(
    dateFrom: string,
    dateTo: string,
    limit: number,
  ): Promise<EventDto[]> {
    const { data } = await http.get<EventDto[]>('/api/events', {
      params: { dateFrom, dateTo, limit },
    });
    return data;
  },
};

