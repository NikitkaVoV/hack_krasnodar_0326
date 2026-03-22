package ru.fshs.tour.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.fshs.tour.controller.dto.landing.EventDto;
import ru.fshs.tour.mapper.LandingMapper;
import ru.fshs.tour.repository.EventRepository;

@Service
@RequiredArgsConstructor
public class LandingEventService {

    private static final int DEFAULT_LIMIT = 6;
    private static final int MAX_LIMIT = 50;

    private final EventRepository eventRepository;
    private final LandingMapper landingMapper;

    public List<EventDto> getUpcomingEvents(LocalDate dateFrom, LocalDate dateTo, int limit) {
        var from = dateFrom != null ? dateFrom : LocalDate.now();
        var to = dateTo != null ? dateTo : from.plusDays(30);
        if (to.isBefore(from)) {
            return List.of();
        }

        var safeLimit = normalizeLimit(limit);
        var fromDateTime = from.atStartOfDay();
        var toExclusive = to.plusDays(1).atStartOfDay();

        return eventRepository.findAllByStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeAsc(
                        fromDateTime,
                        toExclusive,
                        PageRequest.of(0, safeLimit)
                ).stream()
                .map(landingMapper::toEventDto)
                .toList();
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
