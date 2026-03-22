package ru.fshs.tour.assistant.domain.event;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fshs.tour.assistant.domain.common.AssistantResponseType;
import ru.fshs.tour.assistant.domain.common.AssistantSearchTextParser;
import ru.fshs.tour.assistant.domain.common.AssistantSystemResponse;
import ru.fshs.tour.assistant.domain.common.NextAction;
import ru.fshs.tour.assistant.domain.common.ParsedSearchQuery;
import ru.fshs.tour.assistant.model.AssistantStatus;
import ru.fshs.tour.domain.content.Event;
import ru.fshs.tour.repository.EventRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventAssistantServiceImpl implements EventAssistantService {

    private static final int SEARCH_LIMIT = 20;

    private final EventRepository eventRepository;

    @Override
    public AssistantSystemResponse search(String queryText, String locationText) {
        ParsedSearchQuery parsedSearchQuery = AssistantSearchTextParser.parse(queryText, locationText);

        var events = loadEventsForAssistant(parsedSearchQuery, SEARCH_LIMIT).stream()
                .sorted(Comparator.comparing(this::popularityScore).reversed())
                .toList();

        var items = events.stream().map(this::toCard).toList();
        if (items.isEmpty()) {
            return AssistantSystemResponse.builder()
                    .status(AssistantStatus.PARTIAL)
                    .responseType(AssistantResponseType.EVENT_LIST)
                    .summary("События по запросу не найдены.")
                    .warnings(List.of("Попробуйте другой период или уточните тематику."))
                    .nextActions(List.of(NextAction.ASK_CLARIFICATION))
                    .payload(new EventAssistantResult("No events found", List.of()))
                    .build();
        }

        return AssistantSystemResponse.builder()
                .status(AssistantStatus.SUCCESS)
                .responseType(AssistantResponseType.EVENT_LIST)
                .summary("Найдено событий: " + items.size())
                .payload(new EventAssistantResult("Event search completed", items))
                .nextActions(List.of(NextAction.OPEN_EVENT, NextAction.SHOW_ON_MAP))
                .build();
    }

    @Override
    public AssistantSystemResponse explain(UUID eventId) {
        if (eventId == null) {
            return AssistantSystemResponse.builder()
                    .status(AssistantStatus.NEEDS_CLARIFICATION)
                    .responseType(AssistantResponseType.EVENT_DETAILS)
                    .summary("Уточните, какое событие нужно описать.")
                    .nextActions(List.of(NextAction.ASK_CLARIFICATION))
                    .build();
        }

        var event = eventRepository.findDetailedById(eventId).orElse(null);
        if (event == null) {
            return AssistantSystemResponse.builder()
                    .status(AssistantStatus.FAILED)
                    .responseType(AssistantResponseType.EVENT_DETAILS)
                    .summary("Событие не найдено.")
                    .warnings(List.of("Проверьте идентификатор события."))
                    .nextActions(List.of(NextAction.RETRY))
                    .build();
        }

        var card = toCard(event);
        return AssistantSystemResponse.builder()
                .status(AssistantStatus.SUCCESS)
                .responseType(AssistantResponseType.EVENT_DETAILS)
                .summary(event.getName())
                .payload(new EventAssistantResult("Event details", List.of(card)))
                .advice(event.getAdvice() != null && !event.getAdvice().isBlank()
                        ? List.of(event.getAdvice())
                        : List.of())
                .nextActions(List.of(NextAction.OPEN_EVENT, NextAction.SHOW_ON_MAP))
                .build();
    }

    private EventAssistantResult.EventCard toCard(Event event) {
        return new EventAssistantResult.EventCard(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getAddress(),
                event.getLat() != null ? event.getLat().doubleValue() : null,
                event.getLng() != null ? event.getLng().doubleValue() : null,
                event.getStartTime(),
                event.getEndTime(),
                event.getPlace() != null ? event.getPlace().getName() : null,
                event.getEventCategories().stream()
                        .map(eventCategory -> eventCategory.getCategory().getName())
                        .distinct()
                        .toList(),
                event.getEventTags().stream()
                        .map(eventTag -> eventTag.getTag().getName())
                        .distinct()
                        .toList(),
                event.getEventConstraints().stream()
                        .map(eventConstraint -> eventConstraint.getConstraint().getName())
                        .distinct()
                        .toList(),
                event.getPopularity(),
                event.getStatus()
        );
    }

    private Integer popularityScore(Event event) {
        return event.getPopularity() != null ? event.getPopularity() : 0;
    }

    private List<Event> loadEventsForAssistant(ParsedSearchQuery parsedSearchQuery, int limit) {
        var pageable = PageRequest.of(0, Math.max(1, limit));
        if (parsedSearchQuery == null || !parsedSearchQuery.hasTerms()) {
            return eventRepository.findAll(pageable).getContent();
        }

        var deduplicated = new LinkedHashMap<UUID, Event>();
        for (String term : collectSearchTerms(parsedSearchQuery)) {
            var batch = eventRepository.searchForAssistant(term, pageable);
            for (Event event : batch) {
                deduplicated.putIfAbsent(event.getId(), event);
            }
            if (deduplicated.size() >= limit * 3) {
                break;
            }
        }

        return new ArrayList<>(deduplicated.values());
    }

    private List<String> collectSearchTerms(ParsedSearchQuery parsedSearchQuery) {
        var terms = new LinkedHashSet<String>();
        if (parsedSearchQuery.locationText() != null && !parsedSearchQuery.locationText().isBlank()) {
            terms.add(parsedSearchQuery.locationText());
        }
        if (parsedSearchQuery.keywords() != null) {
            for (String keyword : parsedSearchQuery.keywords()) {
                if (keyword != null && !keyword.isBlank()) {
                    terms.add(keyword);
                }
            }
        }
        return List.copyOf(terms);
    }
}
