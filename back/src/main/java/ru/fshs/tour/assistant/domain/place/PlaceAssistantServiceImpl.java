package ru.fshs.tour.assistant.domain.place;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import ru.fshs.tour.domain.content.Place;
import ru.fshs.tour.repository.PlaceRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceAssistantServiceImpl implements PlaceAssistantService {

    private static final int SEARCH_LIMIT = 20;
    private static final int NEARBY_LIMIT = 10;
    private static final double DEFAULT_RADIUS_KM = 5.0d;
    private static final Pattern RESULT_LIMIT_PATTERN = Pattern.compile("\\b([1-9]|[1-4][0-9]|50)\\b");
    private static final Set<String> META_CATEGORY_TERMS = Set.of(
            "popular", "top", "best", "rating", "highest",
            "\u043f\u043e\u043f\u0443\u043b\u044f\u0440\u043d\u044b\u0439", "\u043f\u043e\u043f\u0443\u043b\u044f\u0440\u043d\u044b\u0435", "\u043f\u043e\u043f\u0443\u043b\u044f\u0440\u043d\u044b\u0445",
            "\u0442\u043e\u043f", "\u043b\u0443\u0447\u0448\u0438\u0439", "\u043b\u0443\u0447\u0448\u0438\u0435", "\u043b\u0443\u0447\u0448\u0438\u0445"
    );

    private final PlaceRepository placeRepository;

    @Override
    public AssistantSystemResponse search(PlaceSearchCriteria criteria) {
        var normalizedCriteria = criteria != null ? criteria : PlaceSearchCriteria.builder().build();
        ParsedSearchQuery parsedSearchQuery = AssistantSearchTextParser.parse(
                normalizedCriteria.getQueryText(),
                normalizedCriteria.getLocationText()
        );
        var places = loadPlacesForAssistant(parsedSearchQuery, SEARCH_LIMIT);

        var filtered = places.stream()
                .filter(place -> matchesTags(place, normalizedCriteria.getTags()))
                .filter(place -> matchesCategories(place, normalizedCriteria.getCategories()))
                .filter(place -> matchesConstraints(place, normalizedCriteria.getConstraints()))
                .sorted(Comparator.comparing(this::popularityScore).reversed())
                .toList();

        int resultLimit = resolveRequestedLimit(normalizedCriteria.getQueryText());
        var items = filtered.stream()
                .limit(resultLimit)
                .map(place -> toCard(place, normalizedCriteria.getLat(), normalizedCriteria.getLng()))
                .toList();

        if (items.isEmpty()) {
            return AssistantSystemResponse.builder()
                    .status(AssistantStatus.PARTIAL)
                    .responseType(AssistantResponseType.PLACE_LIST)
                    .summary("По вашему запросу места не найдены.")
                    .warnings(List.of("Попробуйте изменить формулировку или убрать часть фильтров."))
                    .nextActions(List.of(NextAction.ASK_CLARIFICATION))
                    .payload(new PlaceAssistantResult("No places found", List.of()))
                    .build();
        }

        return AssistantSystemResponse.builder()
                .status(AssistantStatus.SUCCESS)
                .responseType(AssistantResponseType.PLACE_LIST)
                .summary("Найдено мест: " + items.size())
                .payload(new PlaceAssistantResult("Place search completed", items))
                .nextActions(List.of(NextAction.SHOW_ON_MAP, NextAction.OPEN_PLACE))
                .build();
    }

    @Override
    public AssistantSystemResponse explain(UUID placeId) {
        if (placeId == null) {
            return AssistantSystemResponse.builder()
                    .status(AssistantStatus.NEEDS_CLARIFICATION)
                    .responseType(AssistantResponseType.PLACE_DETAILS)
                    .summary("Уточните, какое место нужно описать.")
                    .nextActions(List.of(NextAction.ASK_CLARIFICATION))
                    .build();
        }

        var place = placeRepository.findDetailedById(placeId).orElse(null);
        if (place == null) {
            return AssistantSystemResponse.builder()
                    .status(AssistantStatus.FAILED)
                    .responseType(AssistantResponseType.PLACE_DETAILS)
                    .summary("Место не найдено.")
                    .warnings(List.of("Проверьте идентификатор места."))
                    .nextActions(List.of(NextAction.RETRY))
                    .build();
        }

        var card = toCard(place, null, null);
        return AssistantSystemResponse.builder()
                .status(AssistantStatus.SUCCESS)
                .responseType(AssistantResponseType.PLACE_DETAILS)
                .summary(place.getName())
                .payload(new PlaceAssistantResult("Place details", List.of(card)))
                .advice(place.getAdvice() != null && !place.getAdvice().isBlank()
                        ? List.of(place.getAdvice())
                        : List.of())
                .nextActions(List.of(NextAction.OPEN_PLACE, NextAction.SHOW_ON_MAP))
                .build();
    }

    @Override
    public AssistantSystemResponse nearbySuggestions(Double lat, Double lng, double radiusKm) {
        if (lat == null || lng == null) {
            return AssistantSystemResponse.builder()
                    .status(AssistantStatus.NEEDS_CLARIFICATION)
                    .responseType(AssistantResponseType.NEARBY_LIST)
                    .summary("Нужны координаты для подбора мест рядом.")
                    .nextActions(List.of(NextAction.ASK_CLARIFICATION))
                    .build();
        }

        double effectiveRadius = radiusKm > 0 ? radiusKm : DEFAULT_RADIUS_KM;

        var nearby = loadPlacesForAssistant(null, 100).stream()
                .filter(place -> place.getLat() != null && place.getLng() != null)
                .map(place -> toCard(place, lat, lng))
                .filter(card -> card.distanceKm() != null && card.distanceKm() <= effectiveRadius)
                .sorted(Comparator.comparing(PlaceAssistantResult.PlaceCard::distanceKm))
                .limit(NEARBY_LIMIT)
                .toList();

        if (nearby.isEmpty()) {
            return AssistantSystemResponse.builder()
                    .status(AssistantStatus.PARTIAL)
                    .responseType(AssistantResponseType.NEARBY_LIST)
                    .summary("Рядом подходящие места не найдены.")
                    .nextActions(List.of(NextAction.ASK_CLARIFICATION))
                    .payload(new PlaceAssistantResult("Nearby places not found", List.of()))
                    .build();
        }

        return AssistantSystemResponse.builder()
                .status(AssistantStatus.SUCCESS)
                .responseType(AssistantResponseType.NEARBY_LIST)
                .summary("Найдено мест рядом: " + nearby.size())
                .payload(new PlaceAssistantResult("Nearby suggestions", nearby))
                .nextActions(List.of(NextAction.SHOW_ON_MAP, NextAction.OPEN_PLACE))
                .build();
    }

    private PlaceAssistantResult.PlaceCard toCard(Place place, Double originLat, Double originLng) {
        return new PlaceAssistantResult.PlaceCard(
                place.getId(),
                place.getName(),
                firstNonBlank(place.getShortDescription(), place.getDescription()),
                place.getAddress(),
                place.getLat() != null ? place.getLat().doubleValue() : null,
                place.getLng() != null ? place.getLng().doubleValue() : null,
                computeDistanceKm(originLat, originLng, place),
                place.getPlaceCategories().stream()
                        .map(placeCategory -> placeCategory.getCategory().getName())
                        .distinct()
                        .toList(),
                place.getPlaceTags().stream()
                        .map(placeTag -> placeTag.getTag().getName())
                        .distinct()
                        .toList(),
                place.getPlaceConstraints().stream()
                        .map(placeConstraint -> placeConstraint.getConstraint().getName())
                        .distinct()
                        .toList(),
                place.getPopularity(),
                place.getStatus()
        );
    }

    private boolean matchesTags(Place place, List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return true;
        }
        var requested = tags.stream().map(this::normalizeQuery).filter(value -> value != null).toList();
        if (requested.isEmpty()) {
            return true;
        }
        var available = place.getPlaceTags().stream()
                .map(placeTag -> normalizeQuery(placeTag.getTag().getName()))
                .toList();
        return requested.stream().allMatch(available::contains);
    }

    private boolean matchesCategories(Place place, List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            return true;
        }
        var requested = categories.stream()
                .map(this::normalizeQuery)
                .filter(value -> value != null && !META_CATEGORY_TERMS.contains(value))
                .toList();
        if (requested.isEmpty()) {
            return true;
        }
        var available = place.getPlaceCategories().stream()
                .map(placeCategory -> normalizeQuery(placeCategory.getCategory().getName()))
                .toList();
        return requested.stream().allMatch(available::contains);
    }

    private boolean matchesConstraints(Place place, List<String> constraints) {
        if (constraints == null || constraints.isEmpty()) {
            return true;
        }
        var requested = constraints.stream().map(this::normalizeQuery).filter(value -> value != null).toList();
        if (requested.isEmpty()) {
            return true;
        }
        var available = place.getPlaceConstraints().stream()
                .map(placeConstraint -> normalizeQuery(placeConstraint.getConstraint().getName()))
                .toList();
        return requested.stream().allMatch(available::contains);
    }

    private Integer popularityScore(Place place) {
        return place.getPopularity() != null ? place.getPopularity() : 0;
    }

    private String normalizeQuery(String value) {
        if (value == null) {
            return null;
        }
        var normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.isBlank() ? null : normalized;
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }

    private List<Place> loadPlacesForAssistant(ParsedSearchQuery parsedSearchQuery, int limit) {
        var pageable = PageRequest.of(0, Math.max(1, limit));
        if (parsedSearchQuery == null || !parsedSearchQuery.hasTerms()) {
            return placeRepository.findAll(pageable).getContent();
        }

        var deduplicated = new LinkedHashMap<UUID, Place>();
        for (String term : collectSearchTerms(parsedSearchQuery)) {
            var batch = placeRepository.searchForAssistant(term, pageable);
            for (Place place : batch) {
                deduplicated.putIfAbsent(place.getId(), place);
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

    private Double computeDistanceKm(Double originLat, Double originLng, Place place) {
        if (originLat == null || originLng == null || place.getLat() == null || place.getLng() == null) {
            return null;
        }
        return haversineKm(originLat, originLng, place.getLat().doubleValue(), place.getLng().doubleValue());
    }

    private int resolveRequestedLimit(String rawQueryText) {
        String normalized = normalizeQuery(rawQueryText);
        if (normalized == null) {
            return SEARCH_LIMIT;
        }
        Matcher matcher = RESULT_LIMIT_PATTERN.matcher(normalized);
        if (!matcher.find()) {
            return SEARCH_LIMIT;
        }
        return Integer.parseInt(matcher.group(1));
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
}
