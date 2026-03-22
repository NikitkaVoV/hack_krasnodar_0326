package ru.fshs.tour.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fshs.tour.controller.dto.map.MapSearchItemDto;
import ru.fshs.tour.controller.dto.map.MapSearchResponseDto;
import ru.fshs.tour.domain.content.Event;
import ru.fshs.tour.domain.content.Place;
import ru.fshs.tour.repository.EventRepository;
import ru.fshs.tour.repository.PlaceRepository;
import ru.fshs.tour.service.model.MapSearchRequest;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MapSearchService {

    private static final int SHORT_MAX_MINUTES = 120;
    private static final int HALF_DAY_MAX_MINUTES = 300;
    private static final int POPULAR_BADGE_THRESHOLD = 80;
    private static final int HIDDEN_GEM_THRESHOLD = 35;
    private static final int NEW_DAYS = 14;
    private static final Pattern HOURS_RANGE_PATTERN = Pattern.compile("(?<from>\\d{2}:\\d{2})\\s*-\\s*(?<to>\\d{2}:\\d{2})");
    private static final DateTimeFormatter ISO_OFFSET = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final PlaceRepository placeRepository;
    private final EventRepository eventRepository;

    public MapSearchResponseDto search(MapSearchRequest request) {
        validateBudget(request.minBudget(), request.maxBudget());

        var typeFilter = parseType(request.types());
        var durationFilter = parseDuration(request.duration());
        var sortBy = parseSortBy(request.sortBy());
        var categories = parseCsvLower(request.categories());
        var suitableFor = parseSuitableFor(request.suitableFor());
        var reference = resolveReferenceDateTime(request.date(), request.time());

        var items = new ArrayList<MapResultItem>();
        if (typeFilter != TypeFilter.EVENTS) {
            placeRepository.findAll().forEach(place -> toPlaceItem(place, request, reference)
                    .filter(item -> matchesCommonFilters(item, categories, suitableFor, durationFilter))
                    .ifPresent(items::add));
        }
        if (typeFilter != TypeFilter.PLACES) {
            eventRepository.findAll().forEach(event -> toEventItem(event, request, reference)
                    .filter(item -> matchesCommonFilters(item, categories, suitableFor, durationFilter))
                    .ifPresent(items::add));
        }

        items.sort(buildComparator(sortBy, reference, request.radiusKm(), categories));
        var result = items.stream().map(MapResultItem::dto).toList();
        return new MapSearchResponseDto(result, result.size());
    }

    private java.util.Optional<MapResultItem> toPlaceItem(Place place, MapSearchRequest request, LocalDateTime reference) {
        if (place.getLat() == null || place.getLng() == null) {
            return java.util.Optional.empty();
        }
        var distance = haversineKm(request.lat(), request.lng(), place.getLat().doubleValue(), place.getLng().doubleValue());
        if (distance > request.radiusKm()) {
            return java.util.Optional.empty();
        }

        var isOpenNow = isPlaceOpenNow(place, reference);
        if (Boolean.TRUE.equals(request.openNow()) && !isOpenNow) {
            return java.util.Optional.empty();
        }

        var avgBudget = extractPlaceBudget(place);
        if (!matchesBudget(avgBudget, request.minBudget(), request.maxBudget())) {
            return java.util.Optional.empty();
        }

        var category = place.getPlaceCategories().stream()
                .map(pc -> pc.getCategory().getSlug())
                .findFirst()
                .orElse("places");
        var tags = place.getPlaceTags().stream()
                .map(pt -> pt.getTag().getSlug())
                .filter(tag -> tag != null && !tag.isBlank())
                .distinct()
                .toList();
        var popularity = place.getPopularity() != null ? place.getPopularity() : 0;
        var badges = collectBadges(popularity, place.getCreatedAt(), isOpenNow, false);
        var imageUrl = place.getMedia().stream()
                .sorted(Comparator.comparingInt(m -> m.getSortOrder() != null ? m.getSortOrder() : Integer.MAX_VALUE))
                .map(m -> m.getUrl())
                .filter(url -> url != null && !url.isBlank())
                .findFirst()
                .orElse(null);

        var dto = new MapSearchItemDto(
                "p-" + place.getId(),
                "place",
                place.getName(),
                place.getDescription(),
                place.getLat(),
                place.getLng(),
                toScale(distance),
                category,
                tags,
                imageUrl,
                popularity,
                isOpenNow,
                false,
                badges,
                buildPracticalInfo(place, avgBudget),
                avgBudget,
                place.getRecommendedDuration(),
                null,
                null
        );
        return java.util.Optional.of(new MapResultItem(dto, distance, popularity, null, isOpenNow, tags, category));
    }

    private java.util.Optional<MapResultItem> toEventItem(Event event, MapSearchRequest request, LocalDateTime reference) {
        var point = resolveEventPoint(event);
        if (point == null) {
            return java.util.Optional.empty();
        }
        var distance = haversineKm(request.lat(), request.lng(), point.lat(), point.lng());
        if (distance > request.radiusKm()) {
            return java.util.Optional.empty();
        }

        var isOngoing = isEventOngoing(event, reference);
        if (Boolean.TRUE.equals(request.openNow()) && !isOngoing) {
            return java.util.Optional.empty();
        }

        var category = event.getEventCategories().stream()
                .map(ec -> ec.getCategory().getSlug())
                .findFirst()
                .orElse("events");
        var tags = event.getEventTags().stream()
                .map(et -> et.getTag().getSlug())
                .filter(tag -> tag != null && !tag.isBlank())
                .distinct()
                .toList();
        var popularity = event.getPopularity() != null ? event.getPopularity() : 0;
        var badges = collectBadges(popularity, event.getCreatedAt(), false, isOngoing);

        var dto = new MapSearchItemDto(
                "e-" + event.getId(),
                "event",
                event.getName(),
                event.getDescription(),
                BigDecimal.valueOf(point.lat()),
                BigDecimal.valueOf(point.lng()),
                toScale(distance),
                category,
                tags,
                null,
                popularity,
                isOngoing,
                false,
                badges,
                null,
                null,
                event.getRecommendedDuration(),
                toOffsetString(event.getStartTime()),
                toOffsetString(event.getEndTime())
        );
        return java.util.Optional.of(new MapResultItem(
                dto,
                distance,
                popularity,
                event.getStartTime(),
                isOngoing,
                tags,
                category
        ));
    }

    private boolean matchesCommonFilters(
            MapResultItem item,
            Set<String> categories,
            Set<SuitableFor> suitableFor,
            DurationFilter durationFilter
    ) {
        if (!categories.isEmpty() && !matchesCategories(item, categories)) {
            return false;
        }
        if (!suitableFor.isEmpty() && !matchesSuitableFor(item, suitableFor)) {
            return false;
        }
        return matchesDuration(item.dto().avgDurationMinutes(), durationFilter);
    }

    private boolean matchesCategories(MapResultItem item, Set<String> categories) {
        if (categories.contains(normalize(item.primaryCategory()))) {
            return true;
        }
        return item.tags().stream().map(this::normalize).anyMatch(categories::contains);
    }

    private boolean matchesSuitableFor(MapResultItem item, Set<SuitableFor> suitableFor) {
        var haystack = new LinkedHashSet<String>();
        haystack.add(normalize(item.primaryCategory()));
        item.tags().forEach(tag -> haystack.add(normalize(tag)));
        var joined = String.join(" ", haystack);
        for (var target : suitableFor) {
            if (joined.contains(target.token)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesDuration(Integer duration, DurationFilter filter) {
        if (filter == DurationFilter.ANY || duration == null) {
            return true;
        }
        return switch (filter) {
            case SHORT -> duration <= SHORT_MAX_MINUTES;
            case HALF_DAY -> duration > SHORT_MAX_MINUTES && duration <= HALF_DAY_MAX_MINUTES;
            case FULL_DAY -> duration > HALF_DAY_MAX_MINUTES;
            case ANY -> true;
        };
    }

    private Comparator<MapResultItem> buildComparator(
            SortBy sortBy,
            LocalDateTime reference,
            double radiusKm,
            Set<String> categories
    ) {
        return switch (sortBy) {
            case DISTANCE -> Comparator.comparingDouble(MapResultItem::distanceKm)
                    .thenComparing((MapResultItem i) -> i.dto().id());
            case POPULARITY -> Comparator.comparingInt(MapResultItem::popularity).reversed()
                    .thenComparingDouble(MapResultItem::distanceKm);
            case STARTING_SOON -> Comparator
                    .comparing((MapResultItem i) -> startingSoonRank(i, reference))
                    .thenComparing((MapResultItem i) -> timeToStart(i, reference), Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparingDouble(MapResultItem::distanceKm);
            case RELEVANCE -> Comparator.comparingDouble((MapResultItem i) -> relevance(i, radiusKm, categories)).reversed()
                    .thenComparingDouble(MapResultItem::distanceKm);
        };
    }

    private int startingSoonRank(MapResultItem item, LocalDateTime reference) {
        if (!"event".equals(item.dto().type())) {
            return 2;
        }
        if (item.isOpenNow()) {
            return 0;
        }
        return item.eventStartAt() != null && !item.eventStartAt().isBefore(reference) ? 1 : 2;
    }

    private Long timeToStart(MapResultItem item, LocalDateTime reference) {
        if (item.eventStartAt() == null) {
            return null;
        }
        return Math.abs(java.time.Duration.between(reference, item.eventStartAt()).toMinutes());
    }

    private double relevance(MapResultItem item, double radiusKm, Set<String> categories) {
        var normalizedDistance = Math.max(0d, 1d - (item.distanceKm() / Math.max(radiusKm, 0.1d)));
        var popularityScore = Math.min(1d, item.popularity() / 100d);
        var categoryScore = categories.isEmpty() || matchesCategories(item, categories) ? 1d : 0d;
        var openNowScore = item.isOpenNow() ? 1d : 0d;
        return normalizedDistance * 0.45 + popularityScore * 0.35 + categoryScore * 0.15 + openNowScore * 0.05;
    }

    private boolean matchesBudget(Integer avgBudget, Integer minBudget, Integer maxBudget) {
        if (minBudget == null && maxBudget == null) {
            return true;
        }
        if (avgBudget == null) {
            return false;
        }
        if (minBudget != null && avgBudget < minBudget) {
            return false;
        }
        if (maxBudget != null && avgBudget > maxBudget) {
            return false;
        }
        return true;
    }

    private Integer extractPlaceBudget(Place place) {
        return null;
    }

    private String buildPracticalInfo(Place place, Integer avgBudget) {
        var parts = new ArrayList<String>();
        if (avgBudget != null) {
            parts.add("Average budget: " + avgBudget);
        }
        if (place.getOpeningHours() != null && !place.getOpeningHours().isBlank()) {
            parts.add("Open: " + place.getOpeningHours());
        }
        if (place.getAddress() != null && !place.getAddress().isBlank()) {
            parts.add("Address: " + place.getAddress());
        }
        return parts.isEmpty() ? null : String.join(". ", parts);
    }

    private List<String> collectBadges(Integer popularity, java.time.OffsetDateTime createdAt, boolean placeOpenNow, boolean eventOngoing) {
        var badges = new ArrayList<String>();
        if (popularity != null && popularity >= POPULAR_BADGE_THRESHOLD) {
            badges.add("popular");
        }
        if (createdAt != null && createdAt.isAfter(java.time.OffsetDateTime.now().minusDays(NEW_DAYS))) {
            badges.add("new");
        }
        if (popularity != null && popularity <= HIDDEN_GEM_THRESHOLD) {
            badges.add("hidden-gem");
        }
        if (placeOpenNow) {
            badges.add("open-now");
        }
        if (eventOngoing) {
            badges.add("event-ongoing");
        }
        return badges;
    }

    private boolean isPlaceOpenNow(Place place, LocalDateTime reference) {
        var openingHours = place.getOpeningHours();
        if (openingHours == null || openingHours.isBlank()) {
            return false;
        }
        var lower = openingHours.toLowerCase(Locale.ROOT);
        if (lower.contains("24/7") || lower.contains("24h")) {
            return true;
        }
        if (lower.contains("closed") || lower.contains("выход")) {
            return false;
        }

        var matcher = HOURS_RANGE_PATTERN.matcher(openingHours);
        if (!matcher.find()) {
            return true;
        }
        var from = LocalTime.parse(matcher.group("from"));
        var to = LocalTime.parse(matcher.group("to"));
        var current = reference.toLocalTime();
        if (to.isBefore(from)) {
            return !current.isBefore(from) || !current.isAfter(to);
        }
        return !current.isBefore(from) && !current.isAfter(to);
    }

    private boolean isEventOngoing(Event event, LocalDateTime reference) {
        if (event.getStartTime() == null) {
            return false;
        }
        if (event.getEndTime() == null) {
            return !event.getStartTime().isAfter(reference);
        }
        return !event.getStartTime().isAfter(reference) && !event.getEndTime().isBefore(reference);
    }

    private EventPoint resolveEventPoint(Event event) {
        if (event.getLat() != null && event.getLng() != null) {
            return new EventPoint(event.getLat().doubleValue(), event.getLng().doubleValue());
        }
        if (event.getPlace() != null && event.getPlace().getLat() != null && event.getPlace().getLng() != null) {
            return new EventPoint(event.getPlace().getLat().doubleValue(), event.getPlace().getLng().doubleValue());
        }
        return null;
    }

    private String toOffsetString(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toOffsetDateTime().format(ISO_OFFSET);
    }

    private TypeFilter parseType(String raw) {
        if (raw == null || raw.isBlank()) {
            return TypeFilter.BOTH;
        }
        return switch (normalize(raw)) {
            case "both" -> TypeFilter.BOTH;
            case "places" -> TypeFilter.PLACES;
            case "events" -> TypeFilter.EVENTS;
            default -> throw new IllegalArgumentException("types must be one of: both, places, events");
        };
    }

    private DurationFilter parseDuration(String raw) {
        if (raw == null || raw.isBlank()) {
            return DurationFilter.ANY;
        }
        return switch (normalize(raw)) {
            case "short" -> DurationFilter.SHORT;
            case "half-day" -> DurationFilter.HALF_DAY;
            case "full-day" -> DurationFilter.FULL_DAY;
            case "any" -> DurationFilter.ANY;
            default -> throw new IllegalArgumentException("duration must be one of: short, half-day, full-day, any");
        };
    }

    private SortBy parseSortBy(String raw) {
        if (raw == null || raw.isBlank()) {
            return SortBy.RELEVANCE;
        }
        return switch (normalize(raw)) {
            case "distance" -> SortBy.DISTANCE;
            case "popularity" -> SortBy.POPULARITY;
            case "relevance" -> SortBy.RELEVANCE;
            case "starting-soon" -> SortBy.STARTING_SOON;
            default -> throw new IllegalArgumentException("sortBy must be one of: distance, popularity, relevance, starting-soon");
        };
    }

    private Set<SuitableFor> parseSuitableFor(String raw) {
        var values = parseCsvLower(raw);
        if (values.isEmpty()) {
            return EnumSet.noneOf(SuitableFor.class);
        }
        var result = EnumSet.noneOf(SuitableFor.class);
        for (var value : values) {
            result.add(SuitableFor.from(value));
        }
        return result;
    }

    private Set<String> parseCsvLower(String raw) {
        if (raw == null || raw.isBlank()) {
            return Set.of();
        }
        return java.util.Arrays.stream(raw.split(","))
                .map(this::normalize)
                .filter(part -> !part.isBlank())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private LocalDateTime resolveReferenceDateTime(LocalDate date, LocalTime time) {
        if (date == null && time == null) {
            return LocalDateTime.now();
        }
        var actualDate = date != null ? date : LocalDate.now();
        var actualTime = time != null ? time : LocalTime.now();
        return LocalDateTime.of(actualDate, actualTime);
    }

    private void validateBudget(Integer minBudget, Integer maxBudget) {
        if (minBudget != null && minBudget < 0) {
            throw new IllegalArgumentException("minBudget must be >= 0");
        }
        if (maxBudget != null && maxBudget < 0) {
            throw new IllegalArgumentException("maxBudget must be >= 0");
        }
        if (minBudget != null && maxBudget != null && minBudget > maxBudget) {
            throw new IllegalArgumentException("minBudget must be <= maxBudget");
        }
    }

    private BigDecimal toScale(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
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

    private record EventPoint(double lat, double lng) {
    }

    private record MapResultItem(
            MapSearchItemDto dto,
            double distanceKm,
            int popularity,
            LocalDateTime eventStartAt,
            boolean isOpenNow,
            List<String> tags,
            String primaryCategory
    ) {
    }

    private enum TypeFilter {
        BOTH, PLACES, EVENTS
    }

    private enum DurationFilter {
        SHORT, HALF_DAY, FULL_DAY, ANY
    }

    private enum SortBy {
        DISTANCE, POPULARITY, RELEVANCE, STARTING_SOON
    }

    private enum SuitableFor {
        CHILDREN("children"),
        ELDERLY("elderly"),
        SOLO("solo"),
        GROUPS("groups");

        private final String token;

        SuitableFor(String token) {
            this.token = token;
        }

        private static SuitableFor from(String value) {
            return switch (value) {
                case "children" -> CHILDREN;
                case "elderly" -> ELDERLY;
                case "solo" -> SOLO;
                case "groups" -> GROUPS;
                default -> throw new IllegalArgumentException(
                        "suitableFor values must be one of: children, elderly, solo, groups"
                );
            };
        }
    }
}
