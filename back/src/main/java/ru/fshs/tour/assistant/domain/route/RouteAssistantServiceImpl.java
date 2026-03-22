package ru.fshs.tour.assistant.domain.route;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fshs.tour.assistant.domain.common.AssistantResponseType;
import ru.fshs.tour.assistant.domain.common.AssistantSystemResponse;
import ru.fshs.tour.assistant.domain.common.NextAction;
import ru.fshs.tour.assistant.model.AssistantStatus;
import ru.fshs.tour.domain.content.Event;
import ru.fshs.tour.domain.content.Place;
import ru.fshs.tour.domain.route.RouteStep;
import ru.fshs.tour.domain.user.User;
import ru.fshs.tour.repository.EventRepository;
import ru.fshs.tour.repository.PlaceRepository;
import ru.fshs.tour.repository.RouteRepository;
import ru.fshs.tour.repository.RouteStepRepository;
import ru.fshs.tour.repository.UserRepository;
import ru.fshs.tour.service.ai.IntentParserService;
import ru.fshs.tour.service.ai.RecommendationService;
import ru.fshs.tour.service.ai.RouteBuilderService;
import ru.fshs.tour.service.ai.model.GeneratedRouteStep;
import ru.fshs.tour.service.ai.model.ParsedIntent;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RouteAssistantServiceImpl implements RouteAssistantService {

    private static final int DEFAULT_COUNT = 3;
    private static final int MIN_COUNT = 1;
    private static final int MAX_COUNT = 8;
    private static final double DEFAULT_RADIUS_KM = 25.0d;
    private static final Pattern COUNT_PATTERN = Pattern.compile(
            "(?iu)(?:из|from)\\s*(\\d{1,2})|\\b(\\d{1,2})\\s*(?:мест|точк|виноделен|винодельни|локац|places?)\\b"
    );
    private static final Pattern RADIUS_PATTERN = Pattern.compile("(?iu)(\\d{1,3})\\s*(?:км|km)");

    private static final Map<String, List<String>> CATEGORY_KEYWORDS = Map.of(
            "winery", List.of("винодель", "winery", "wine"),
            "farm", List.of("ферм", "farm"),
            "gastro", List.of("гастро", "ресторан", "restaurant", "кафе", "cafe"),
            "nature", List.of("природ", "парк", "лес", "nature", "park"),
            "event", List.of("событ", "концерт", "выставк", "event"),
            "animals", List.of("живот", "зоо", "zoo", "safari")
    );

    private static final Map<String, List<String>> CATEGORY_ALTERNATIVES = Map.of(
            "winery", List.of("farm", "gastro", "nature"),
            "farm", List.of("nature", "gastro"),
            "gastro", List.of("winery", "nature"),
            "nature", List.of("farm", "event"),
            "event", List.of("nature", "gastro"),
            "animals", List.of("nature", "farm")
    );

    private final PlaceRepository placeRepository;
    private final EventRepository eventRepository;
    private final RouteRepository routeRepository;
    private final RouteStepRepository routeStepRepository;
    private final UserRepository userRepository;
    private final IntentParserService intentParserService;
    private final RouteBuilderService routeBuilderService;
    private final RecommendationService recommendationService;

    @Override
    public AssistantSystemResponse buildRoute(RouteBuildCommand command) {
        var message = command.sourceMessage() != null ? command.sourceMessage() : "";
        var user = command.userId() != null ? userRepository.findById(command.userId()).orElse(null) : null;
        var baseIntent = intentParserService.parse(message);
        var requestedCount = extractRequestedCount(message);
        var requestedCategories = parseRequestedCategories(message);
        var nearUser = isNearbyRequest(message);
        var searchRadiusKm = extractRadiusKm(message);

        var allPlaces = placeRepository.findAll().stream()
                .filter(this::isAvailablePlace)
                .toList();
        var allEvents = eventRepository.findAll().stream()
                .filter(this::isAvailableEvent)
                .toList();

        var unavailable = recommendationService.analyzeRequest(baseIntent.requestedActivities(), allPlaces, allEvents);

        var normalizedIntent = mergeUserContext(baseIntent, user);
        var filtered = filterCandidates(
                allPlaces,
                allEvents,
                normalizedIntent,
                requestedCategories,
                nearUser,
                user,
                searchRadiusKm
        );

        var warnings = new ArrayList<String>(filtered.warnings());
        var suggestions = new LinkedHashSet<String>(filtered.suggestions());
        suggestions.addAll(unavailable.alternatives());

        var generated = routeBuilderService.buildRoute(normalizedIntent, filtered.places(), filtered.events());
        warnings.addAll(generated.warnings());

        if (generated.isEmpty() && !requestedCategories.isEmpty()) {
            var fallbackCategories = collectAlternativeCategories(requestedCategories);
            if (!fallbackCategories.isEmpty()) {
                var alternativeFiltered = filterCandidates(
                        allPlaces,
                        allEvents,
                        normalizedIntent,
                        fallbackCategories,
                        nearUser,
                        user,
                        searchRadiusKm
                );
                warnings.add("По точному запросу найдено недостаточно точек, добавлены близкие альтернативы.");
                suggestions.add("Расширить радиус поиска");
                suggestions.add("Собрать маршрут со смежными категориями");
                generated = routeBuilderService.buildRoute(normalizedIntent, alternativeFiltered.places(), alternativeFiltered.events());
                warnings.addAll(alternativeFiltered.warnings());
                warnings.addAll(generated.warnings());
            }
        }

        if (generated.isEmpty()) {
            generated = routeBuilderService.buildFallbackRoute(normalizedIntent);
            warnings.addAll(generated.warnings());
            suggestions.add("Расширить радиус поиска");
            suggestions.add("Уточнить категорию или длительность маршрута");
        }

        var limitedSteps = generated.steps().stream().limit(requestedCount).toList();
        if (limitedSteps.size() < requestedCount) {
            warnings.add("Найдено меньше точек, чем вы запросили: " + limitedSteps.size() + " из " + requestedCount + ".");
            suggestions.add("Уменьшить количество точек в маршруте");
            suggestions.add("Добавить смежные категории (фермы, гастро-точки, природа)");
        }

        var placesById = allPlaces.stream().collect(java.util.stream.Collectors.toMap(Place::getId, value -> value, (a, b) -> a));
        var eventsById = allEvents.stream().collect(java.util.stream.Collectors.toMap(Event::getId, value -> value, (a, b) -> a));
        var routeSteps = IntStream.range(0, limitedSteps.size())
                .mapToObj(index -> {
                    var step = limitedSteps.get(index);
                    return withOrder(
                            toAssistantStep(step, placesById.get(step.id()), eventsById.get(step.id())),
                            index + 1
                    );
                })
                .toList();

        var routeDate = normalizedIntent.date() != null ? normalizedIntent.date() : LocalDate.now();
        var totalDuration = routeSteps.stream().map(RouteAssistantResult.AssistantRouteStep::duration)
                .filter(duration -> duration != null && duration > 0)
                .mapToInt(Integer::intValue)
                .sum();
        var canBeSaved = !routeSteps.isEmpty();

        var summary = buildRouteSummary(routeSteps, requestedCategories, routeDate, totalDuration);
        var advice = buildRouteAdvice(routeSteps, nearUser, unavailable.hasUnavailableActivities());
        var imageUrl = routeSteps.stream()
                .map(RouteAssistantResult.AssistantRouteStep::imageUrl)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(null);

        var routePayload = new RouteAssistantResult.AssistantRoute(
                null,
                summary,
                advice,
                totalDuration,
                routeDate,
                imageUrl,
                routeSteps,
                canBeSaved
        );

        var payload = new RouteAssistantResult(
                "Маршрут подготовлен на основе данных платформы.",
                null,
                routePayload,
                List.copyOf(suggestions)
        );

        var responseText = buildHumanText(
                routePayload,
                requestedCount,
                requestedCategories,
                unavailable.unavailableActivities(),
                List.copyOf(warnings)
        );

        return AssistantSystemResponse.builder()
                .status(canBeSaved ? AssistantStatus.SUCCESS : AssistantStatus.PARTIAL)
                .responseType(AssistantResponseType.ROUTE_PROPOSAL)
                .summary(responseText)
                .payload(payload)
                .warnings(List.copyOf(new LinkedHashSet<>(warnings)))
                .advice(List.of(advice))
                .nextActions(canBeSaved
                        ? List.of(NextAction.SAVE_ROUTE, NextAction.SHOW_ON_MAP, NextAction.REBUILD_ROUTE)
                        : List.of(NextAction.REBUILD_ROUTE, NextAction.ASK_CLARIFICATION))
                .build();
    }

    @Override
    public AssistantSystemResponse editRoute(RouteEditCommand command) {
        if (command.routeId() == null) {
            return AssistantSystemResponse.builder()
                    .status(AssistantStatus.NEEDS_CLARIFICATION)
                    .responseType(AssistantResponseType.ROUTE_UPDATED)
                    .summary("Уточните маршрут, который нужно изменить, и что именно поправить.")
                    .nextActions(List.of(NextAction.ASK_CLARIFICATION))
                    .build();
        }

        var route = routeRepository.findById(command.routeId()).orElse(null);
        if (route == null) {
            return AssistantSystemResponse.builder()
                    .status(AssistantStatus.FAILED)
                    .responseType(AssistantResponseType.ROUTE_UPDATED)
                    .summary("Маршрут не найден. Проверьте идентификатор и попробуйте снова.")
                    .nextActions(List.of(NextAction.RETRY))
                    .build();
        }

        return AssistantSystemResponse.builder()
                .status(AssistantStatus.PARTIAL)
                .responseType(AssistantResponseType.ROUTE_UPDATED)
                .summary("Я зафиксировал запрос на изменение маршрута. Сейчас доступен быстрый пересбор с новыми параметрами через чат.")
                .payload(new RouteAssistantResult(
                        "Редактирование выполняется через пересбор маршрута с уточнениями пользователя.",
                        route.getId()
                ))
                .nextActions(List.of(NextAction.REBUILD_ROUTE, NextAction.ASK_CLARIFICATION))
                .build();
    }

    @Override
    public AssistantSystemResponse explainRoute(UUID routeId) {
        if (routeId == null) {
            return AssistantSystemResponse.builder()
                    .status(AssistantStatus.NEEDS_CLARIFICATION)
                    .responseType(AssistantResponseType.ROUTE_DETAILS)
                    .summary("Уточните, какой маршрут нужно описать.")
                    .nextActions(List.of(NextAction.ASK_CLARIFICATION))
                    .build();
        }

        var route = routeRepository.findById(routeId).orElse(null);
        if (route == null) {
            return AssistantSystemResponse.builder()
                    .status(AssistantStatus.FAILED)
                    .responseType(AssistantResponseType.ROUTE_DETAILS)
                    .summary("Маршрут не найден.")
                    .nextActions(List.of(NextAction.RETRY))
                    .build();
        }

        var steps = routeStepRepository.findAllByRouteIdOrderByStepOrderAsc(routeId);
        if (steps.isEmpty()) {
            return AssistantSystemResponse.builder()
                    .status(AssistantStatus.PARTIAL)
                    .responseType(AssistantResponseType.ROUTE_DETAILS)
                    .summary("Маршрут найден, но в нём пока нет шагов.")
                    .payload(new RouteAssistantResult("Маршрут без шагов.", routeId))
                    .nextActions(List.of(NextAction.REBUILD_ROUTE))
                    .build();
        }

        var placeIds = steps.stream()
                .filter(step -> isPlaceType(step.getTargetType()))
                .map(RouteStep::getTargetId)
                .distinct()
                .toList();
        var eventIds = steps.stream()
                .filter(step -> isEventType(step.getTargetType()))
                .map(RouteStep::getTargetId)
                .distinct()
                .toList();
        var placesById = placeRepository.findAllById(placeIds).stream()
                .collect(java.util.stream.Collectors.toMap(Place::getId, value -> value));
        var eventsById = eventRepository.findAllById(eventIds).stream()
                .collect(java.util.stream.Collectors.toMap(Event::getId, value -> value));

        var assistantSteps = new ArrayList<RouteAssistantResult.AssistantRouteStep>();
        for (var step : steps) {
            if (isPlaceType(step.getTargetType())) {
                var place = placesById.get(step.getTargetId());
                if (place == null) {
                    continue;
                }
                assistantSteps.add(toAssistantStep(new GeneratedRouteStep(
                        "place",
                        place.getId(),
                        place.getName(),
                        normalizeDuration(place.getRecommendedDuration()),
                        step.getPlannedTimeStart(),
                        step.getPlannedTimeEnd(),
                        place.getLat(),
                        place.getLng(),
                        place.getAddress()
                ), place, null));
                continue;
            }

            if (isEventType(step.getTargetType())) {
                var event = eventsById.get(step.getTargetId());
                if (event == null) {
                    continue;
                }
                assistantSteps.add(toAssistantStep(new GeneratedRouteStep(
                        "event",
                        event.getId(),
                        event.getName(),
                        normalizeDuration(event.getRecommendedDuration()),
                        step.getPlannedTimeStart(),
                        step.getPlannedTimeEnd(),
                        event.getLat(),
                        event.getLng(),
                        event.getAddress()
                ), null, event));
            }
        }

        var totalDuration = assistantSteps.stream()
                .map(RouteAssistantResult.AssistantRouteStep::duration)
                .filter(duration -> duration != null)
                .mapToInt(Integer::intValue)
                .sum();
        var summary = route.getSummary() != null && !route.getSummary().isBlank()
                ? route.getSummary()
                : "Сохранённый маршрут пользователя";
        var advice = route.getAdvice() != null && !route.getAdvice().isBlank()
                ? route.getAdvice()
                : "Маршрут можно доработать по вашим пожеланиям.";

        var routePayload = new RouteAssistantResult.AssistantRoute(
                route.getId(),
                summary,
                advice,
                route.getTotalDuration() != null ? route.getTotalDuration() : totalDuration,
                route.getDate(),
                assistantSteps.stream()
                        .map(RouteAssistantResult.AssistantRouteStep::imageUrl)
                        .filter(value -> value != null && !value.isBlank())
                        .findFirst()
                        .orElse(null),
                assistantSteps,
                false
        );

        return AssistantSystemResponse.builder()
                .status(AssistantStatus.SUCCESS)
                .responseType(AssistantResponseType.ROUTE_DETAILS)
                .summary("Маршрут содержит " + assistantSteps.size() + " точек. Длительность около "
                        + (routePayload.totalDuration() != null ? routePayload.totalDuration() : 0) + " минут.")
                .payload(new RouteAssistantResult("Детали маршрута загружены.", route.getId(), routePayload, List.of()))
                .nextActions(List.of(NextAction.SHOW_ON_MAP))
                .build();
    }

    private ParsedIntent mergeUserContext(ParsedIntent baseIntent, User user) {
        var preferences = new LinkedHashSet<String>();
        if (baseIntent.preferences() != null) {
            preferences.addAll(baseIntent.preferences());
        }
        if (user != null) {
            user.getUserTags().stream()
                    .map(userTag -> normalize(userTag.getTag().getName()))
                    .filter(tag -> tag != null)
                    .map(this::mapUserTokenToPreference)
                    .filter(value -> value != null)
                    .forEach(preferences::add);
        }

        var constraints = new LinkedHashSet<String>();
        if (baseIntent.constraints() != null) {
            constraints.addAll(baseIntent.constraints());
        }
        if (user != null) {
            user.getUserConstraints().stream()
                    .map(constraint -> normalize(constraint.getConstraint().getName()))
                    .filter(value -> value != null)
                    .map(this::mapConstraint)
                    .filter(value -> value != null)
                    .forEach(constraints::add);
        }

        return new ParsedIntent(
                baseIntent.intent(),
                baseIntent.originalMessage(),
                baseIntent.location(),
                baseIntent.date() != null ? baseIntent.date() : LocalDate.now(),
                baseIntent.durationMinutes(),
                List.copyOf(preferences),
                List.copyOf(constraints),
                baseIntent.budget(),
                baseIntent.requestedActivities()
        );
    }

    private CandidateFilterResult filterCandidates(
            List<Place> places,
            List<Event> events,
            ParsedIntent intent,
            Set<String> requestedCategories,
            boolean nearUser,
            User user,
            double radiusKm
    ) {
        var warnings = new ArrayList<String>();
        var suggestions = new LinkedHashSet<String>();
        var routeDate = intent.date() != null ? intent.date() : LocalDate.now();

        var byCategoryPlaces = places.stream()
                .filter(place -> matchesRequestedCategories(place, requestedCategories))
                .toList();
        var byCategoryEvents = events.stream()
                .filter(event -> matchesRequestedCategories(event, requestedCategories))
                .filter(event -> isEventOnDate(event, routeDate))
                .toList();

        var byLocationPlaces = byCategoryPlaces;
        var byLocationEvents = byCategoryEvents;

        if (intent.location() != null && !intent.location().isBlank()) {
            var normalizedLocation = normalize(intent.location());
            byLocationPlaces = byLocationPlaces.stream()
                    .filter(place -> containsAny(normalizedLocation, place.getName(), place.getAddress(), place.getDescription()))
                    .toList();
            byLocationEvents = byLocationEvents.stream()
                    .filter(event -> containsAny(
                            normalizedLocation,
                            event.getName(),
                            event.getAddress(),
                            event.getDescription(),
                            event.getPlace() != null ? event.getPlace().getAddress() : null
                    ))
                    .toList();
            if (byLocationPlaces.isEmpty() && byLocationEvents.isEmpty()) {
                warnings.add("По указанной локации точных совпадений не найдено, использованы общие варианты.");
                suggestions.add("Уточнить город или район");
                byLocationPlaces = byCategoryPlaces;
                byLocationEvents = byCategoryEvents;
            }
        }

        if (nearUser) {
            var origin = resolveUserOrigin(user);
            if (origin == null) {
                warnings.add("Не удалось определить ваше местоположение, маршрут собран по популярным точкам.");
                suggestions.add("Разрешить доступ к геолокации в профиле");
            } else {
                var finalByLocationPlaces = byLocationPlaces.stream()
                        .filter(place -> distanceFromOrigin(origin, place.getLat(), place.getLng()) <= radiusKm)
                        .sorted(Comparator.comparingDouble(place -> distanceFromOrigin(origin, place.getLat(), place.getLng())))
                        .toList();
                var finalByLocationEvents = byLocationEvents.stream()
                        .filter(event -> distanceFromOrigin(origin, eventLat(event), eventLng(event)) <= radiusKm)
                        .sorted(Comparator.comparingDouble(event -> distanceFromOrigin(origin, eventLat(event), eventLng(event))))
                        .toList();

                byLocationPlaces = finalByLocationPlaces;
                byLocationEvents = finalByLocationEvents;
                if (byLocationPlaces.isEmpty() && byLocationEvents.isEmpty()) {
                    warnings.add("Рядом с вашим местоположением ничего не найдено в текущем радиусе.");
                    suggestions.add("Увеличить радиус поиска");
                }
            }
        }

        if (byLocationPlaces.isEmpty() && byLocationEvents.isEmpty()) {
            warnings.add("Используются популярные точки по базе, так как точные фильтры дали пустой результат.");
            suggestions.add("Снизить количество ограничений");
            return new CandidateFilterResult(places, events, warnings, suggestions);
        }

        return new CandidateFilterResult(byLocationPlaces, byLocationEvents, warnings, suggestions);
    }

    private RouteAssistantResult.AssistantRouteStep toAssistantStep(
            GeneratedRouteStep generatedStep,
            Place place,
            Event event
    ) {
        String description = null;
        String address = generatedStep.address();
        Double lat = generatedStep.lat() != null ? generatedStep.lat().doubleValue() : null;
        Double lng = generatedStep.lng() != null ? generatedStep.lng().doubleValue() : null;
        String imageUrl = null;

        if (place != null) {
            description = firstNonBlank(place.getShortDescription(), place.getDescription());
            address = firstNonBlank(address, place.getAddress());
            lat = lat != null ? lat : (place.getLat() != null ? place.getLat().doubleValue() : null);
            lng = lng != null ? lng : (place.getLng() != null ? place.getLng().doubleValue() : null);
            imageUrl = place.getMedia().stream()
                    .sorted(Comparator.comparingInt(media -> media.getSortOrder() != null ? media.getSortOrder() : Integer.MAX_VALUE))
                    .map(media -> media.getUrl())
                    .filter(url -> url != null && !url.isBlank())
                    .findFirst()
                    .orElse(null);
        }

        if (event != null) {
            description = firstNonBlank(description, event.getDescription());
            address = firstNonBlank(address, event.getAddress(), event.getPlace() != null ? event.getPlace().getAddress() : null);
            lat = lat != null ? lat : eventLat(event);
            lng = lng != null ? lng : eventLng(event);
            if (event.getPlace() != null) {
                imageUrl = event.getPlace().getMedia().stream()
                        .sorted(Comparator.comparingInt(media -> media.getSortOrder() != null ? media.getSortOrder() : Integer.MAX_VALUE))
                        .map(media -> media.getUrl())
                        .filter(url -> url != null && !url.isBlank())
                        .findFirst()
                        .orElse(imageUrl);
            }
        }

        return new RouteAssistantResult.AssistantRouteStep(
                generatedStep.id() != null ? generatedStep.id().toString() : null,
                generatedStep.type(),
                generatedStep.title(),
                description,
                address,
                lat,
                lng,
                generatedStep.durationMinutes(),
                null,
                imageUrl
        );
    }

    private RouteAssistantResult.AssistantRouteStep withOrder(
            RouteAssistantResult.AssistantRouteStep step,
            int orderIndex
    ) {
        return new RouteAssistantResult.AssistantRouteStep(
                step.id(),
                step.type(),
                step.title(),
                step.description(),
                step.address(),
                step.lat(),
                step.lng(),
                step.duration(),
                orderIndex,
                step.imageUrl()
        );
    }

    private String buildRouteSummary(
            List<RouteAssistantResult.AssistantRouteStep> steps,
            Set<String> categories,
            LocalDate date,
            int totalDuration
    ) {
        if (steps.isEmpty()) {
            return "Не удалось собрать полноценный маршрут по вашему запросу.";
        }
        var categoryLabel = categories.isEmpty()
                ? "интересным местам"
                : categoryLabel(categories);
        return "Маршрут на %s по %s".formatted(date, categoryLabel)
                + " (" + steps.size() + " точк., около " + totalDuration + " мин.)";
    }

    private String buildRouteAdvice(
            List<RouteAssistantResult.AssistantRouteStep> steps,
            boolean nearUser,
            boolean hasUnavailableActivities
    ) {
        if (steps.isEmpty()) {
            return "Попробуйте расширить радиус или изменить категорию маршрута.";
        }
        var parts = new ArrayList<String>();
        parts.add("Лучше выезжать в первой половине дня, чтобы пройти маршрут без спешки.");
        if (nearUser) {
            parts.add("Маршрут отсортирован с учётом близости к вашему текущему положению.");
        }
        if (hasUnavailableActivities) {
            parts.add("Часть запрошенных активностей недоступна, поэтому предложены ближайшие альтернативы.");
        }
        return String.join(" ", parts);
    }

    private String buildHumanText(
            RouteAssistantResult.AssistantRoute route,
            int requestedCount,
            Set<String> categories,
            List<String> unavailableActivities,
            List<String> warnings
    ) {
        if (route == null || route.steps().isEmpty()) {
            return "Мне не удалось собрать полноценный маршрут по вашему запросу. "
                    + "Попробуйте уточнить категорию, увеличить радиус или уменьшить количество точек.";
        }

        var text = new StringBuilder();
        if (route.steps().size() >= requestedCount) {
            text.append("Я подобрал для вас маршрут из ").append(route.steps().size()).append(" точек");
        } else {
            text.append("Я нашёл ").append(route.steps().size()).append(" точек из запрошенных ")
                    .append(requestedCount).append(", чтобы не добавлять вымышленные места");
        }
        if (!categories.isEmpty()) {
            text.append(" по категории «").append(categoryLabel(categories)).append("»");
        }
        text.append(". ");

        var highlights = route.steps().stream()
                .limit(3)
                .map(RouteAssistantResult.AssistantRouteStep::title)
                .toList();
        if (!highlights.isEmpty()) {
            text.append("В маршруте: ").append(String.join(", ", highlights)).append(". ");
        }

        text.append("Общая длительность около ").append(route.totalDuration() != null ? route.totalDuration() : 0).append(" минут.");

        if (unavailableActivities != null && !unavailableActivities.isEmpty()) {
            text.append(" Недоступные активности: ").append(String.join(", ", unavailableActivities)).append(".");
        }

        if (warnings != null && !warnings.isEmpty()) {
            text.append(" ").append(warnings.get(0));
        }

        return text.toString();
    }

    private Set<String> parseRequestedCategories(String rawMessage) {
        var normalized = normalize(rawMessage);
        var categories = new LinkedHashSet<String>();
        if (normalized == null) {
            return categories;
        }

        for (var entry : CATEGORY_KEYWORDS.entrySet()) {
            for (var keyword : entry.getValue()) {
                if (normalized.contains(keyword)) {
                    categories.add(entry.getKey());
                    break;
                }
            }
        }
        return categories;
    }

    private Set<String> collectAlternativeCategories(Set<String> requested) {
        var alternatives = new LinkedHashSet<String>();
        for (var category : requested) {
            alternatives.addAll(CATEGORY_ALTERNATIVES.getOrDefault(category, List.of()));
        }
        return alternatives;
    }

    private int extractRequestedCount(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) {
            return DEFAULT_COUNT;
        }
        var matcher = COUNT_PATTERN.matcher(rawMessage);
        while (matcher.find()) {
            var group = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            if (group != null) {
                try {
                    var value = Integer.parseInt(group);
                    return Math.max(MIN_COUNT, Math.min(MAX_COUNT, value));
                } catch (NumberFormatException ignored) {
                    // no-op
                }
            }
        }
        return DEFAULT_COUNT;
    }

    private double extractRadiusKm(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) {
            return DEFAULT_RADIUS_KM;
        }
        var matcher = RADIUS_PATTERN.matcher(rawMessage);
        if (!matcher.find()) {
            return DEFAULT_RADIUS_KM;
        }
        try {
            var value = Integer.parseInt(matcher.group(1));
            return Math.max(1, Math.min(200, value));
        } catch (NumberFormatException ignored) {
            return DEFAULT_RADIUS_KM;
        }
    }

    private boolean isNearbyRequest(String rawMessage) {
        var normalized = normalize(rawMessage);
        if (normalized == null) {
            return false;
        }
        return normalized.contains("недалеко")
                || normalized.contains("рядом")
                || normalized.contains("поблизости")
                || normalized.contains("около меня")
                || normalized.contains("near me")
                || normalized.contains("nearby");
    }

    private boolean matchesRequestedCategories(Place place, Set<String> categories) {
        if (categories.isEmpty()) {
            return true;
        }
        var haystack = collectPlaceTokens(place);
        for (var category : categories) {
            for (var keyword : CATEGORY_KEYWORDS.getOrDefault(category, List.of())) {
                if (haystack.stream().anyMatch(token -> token.contains(keyword) || keyword.contains(token))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matchesRequestedCategories(Event event, Set<String> categories) {
        if (categories.isEmpty()) {
            return true;
        }
        var haystack = collectEventTokens(event);
        for (var category : categories) {
            for (var keyword : CATEGORY_KEYWORDS.getOrDefault(category, List.of())) {
                if (haystack.stream().anyMatch(token -> token.contains(keyword) || keyword.contains(token))) {
                    return true;
                }
            }
        }
        return false;
    }

    private Set<String> collectPlaceTokens(Place place) {
        var tokens = new LinkedHashSet<String>();
        addTokens(tokens, place.getName());
        addTokens(tokens, place.getDescription());
        addTokens(tokens, place.getShortDescription());
        addTokens(tokens, place.getAddress());
        place.getPlaceCategories().forEach(category -> addTokens(tokens, category.getCategory().getName()));
        place.getPlaceTags().forEach(tag -> addTokens(tokens, tag.getTag().getName()));
        return tokens;
    }

    private Set<String> collectEventTokens(Event event) {
        var tokens = new LinkedHashSet<String>();
        addTokens(tokens, event.getName());
        addTokens(tokens, event.getDescription());
        addTokens(tokens, event.getAddress());
        event.getEventCategories().forEach(category -> addTokens(tokens, category.getCategory().getName()));
        event.getEventTags().forEach(tag -> addTokens(tokens, tag.getTag().getName()));
        if (event.getPlace() != null) {
            addTokens(tokens, event.getPlace().getName());
            addTokens(tokens, event.getPlace().getAddress());
        }
        return tokens;
    }

    private void addTokens(Set<String> target, String raw) {
        if (raw == null || raw.isBlank()) {
            return;
        }
        for (var token : raw.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{Nd}]+")) {
            if (token.length() >= 3) {
                target.add(token);
            }
        }
    }

    private boolean isEventOnDate(Event event, LocalDate date) {
        if (event.getStartTime() == null) {
            return false;
        }
        var startDate = event.getStartTime().toLocalDate();
        var endDate = event.getEndTime() != null ? event.getEndTime().toLocalDate() : startDate;
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    private OriginPoint resolveUserOrigin(User user) {
        if (user == null || user.getLastLocationLat() == null || user.getLastLocationLng() == null) {
            return null;
        }
        return new OriginPoint(user.getLastLocationLat(), user.getLastLocationLng());
    }

    private double distanceFromOrigin(OriginPoint origin, BigDecimal lat, BigDecimal lng) {
        if (origin == null || lat == null || lng == null) {
            return Double.MAX_VALUE;
        }
        return haversineKm(origin.lat.doubleValue(), origin.lng.doubleValue(), lat.doubleValue(), lng.doubleValue());
    }

    private double distanceFromOrigin(OriginPoint origin, Double lat, Double lng) {
        if (origin == null || lat == null || lng == null) {
            return Double.MAX_VALUE;
        }
        return haversineKm(origin.lat.doubleValue(), origin.lng.doubleValue(), lat, lng);
    }

    private Double eventLat(Event event) {
        if (event.getLat() != null) {
            return event.getLat().doubleValue();
        }
        if (event.getPlace() != null && event.getPlace().getLat() != null) {
            return event.getPlace().getLat().doubleValue();
        }
        return null;
    }

    private Double eventLng(Event event) {
        if (event.getLng() != null) {
            return event.getLng().doubleValue();
        }
        if (event.getPlace() != null && event.getPlace().getLng() != null) {
            return event.getPlace().getLng().doubleValue();
        }
        return null;
    }

    private String categoryLabel(Set<String> categories) {
        if (categories.contains("winery")) {
            return "винодельням";
        }
        if (categories.contains("farm")) {
            return "фермам";
        }
        if (categories.contains("gastro")) {
            return "гастрономическим точкам";
        }
        if (categories.contains("nature")) {
            return "природным местам";
        }
        if (categories.contains("event")) {
            return "событиям";
        }
        if (categories.contains("animals")) {
            return "локациям с животными";
        }
        return "подходящим локациям";
    }

    private boolean containsAny(String needle, String... values) {
        if (needle == null || needle.isBlank()) {
            return false;
        }
        for (var value : values) {
            if (value != null && value.toLowerCase(Locale.ROOT).contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private String mapUserTokenToPreference(String token) {
        if (token == null) {
            return null;
        }
        if (token.contains("природ") || token.contains("nature") || token.contains("park")) {
            return "nature";
        }
        if (token.contains("живот") || token.contains("zoo") || token.contains("animal")) {
            return "animals";
        }
        if (token.contains("истор") || token.contains("museum")) {
            return "history";
        }
        if (token.contains("культур") || token.contains("театр") || token.contains("art")) {
            return "culture";
        }
        if (token.contains("гастро") || token.contains("еда") || token.contains("food")) {
            return "food";
        }
        return null;
    }

    private String mapConstraint(String token) {
        if (token == null) {
            return null;
        }
        if (token.contains("коляск") || token.contains("wheelchair")) {
            return "wheelchair_access";
        }
        if (token.contains("без долг") || token.contains("no long walking")) {
            return "no_long_walking";
        }
        if (token.contains("дет") || token.contains("family")) {
            return "family_friendly";
        }
        return null;
    }

    private boolean isAvailablePlace(Place place) {
        return place != null
                && place.getId() != null
                && place.getName() != null
                && !place.getName().isBlank()
                && isActiveStatus(place.getStatus());
    }

    private boolean isAvailableEvent(Event event) {
        return event != null
                && event.getId() != null
                && event.getName() != null
                && !event.getName().isBlank()
                && event.getStartTime() != null
                && isActiveStatus(event.getStatus());
    }

    private boolean isActiveStatus(String status) {
        if (status == null || status.isBlank()) {
            return true;
        }
        var normalized = status.toLowerCase(Locale.ROOT);
        return !Set.of("deleted", "archived", "inactive", "draft").contains(normalized);
    }

    private boolean isPlaceType(String raw) {
        return raw != null && raw.toLowerCase(Locale.ROOT).contains("place");
    }

    private boolean isEventType(String raw) {
        return raw != null && raw.toLowerCase(Locale.ROOT).contains("event");
    }

    private int normalizeDuration(Integer value) {
        if (value == null) {
            return 60;
        }
        return Math.max(45, value);
    }

    private String firstNonBlank(String... values) {
        for (var value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        var normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.isBlank() ? null : normalized;
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }

    private record CandidateFilterResult(
            List<Place> places,
            List<Event> events,
            List<String> warnings,
            Set<String> suggestions
    ) {
    }

    private record OriginPoint(
            BigDecimal lat,
            BigDecimal lng
    ) {
    }
}

