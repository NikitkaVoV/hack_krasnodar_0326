package ru.fshs.tour.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.fshs.tour.controller.dto.route_steps.RouteStepEventDto;
import ru.fshs.tour.controller.dto.route_steps.RouteStepPlaceDto;
import ru.fshs.tour.domain.content.Event;
import ru.fshs.tour.domain.content.Place;
import ru.fshs.tour.exception.InvalidIdFormatException;
import ru.fshs.tour.repository.EventRepository;
import ru.fshs.tour.repository.PlaceRepository;

@Service
@Transactional(readOnly = true)
public class RouteStepTargetService {

    private static final Pattern HOURS_RANGE_PATTERN = Pattern.compile("(?<from>\\d{2}:\\d{2})\\s*-\\s*(?<to>\\d{2}:\\d{2})");
    private static final DateTimeFormatter ISO_OFFSET = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final PlaceRepository placeRepository;
    private final EventRepository eventRepository;
    private final ImageCompatService imageCompatService;

    public RouteStepTargetService(
            PlaceRepository placeRepository,
            EventRepository eventRepository,
            ImageCompatService imageCompatService
    ) {
        this.placeRepository = placeRepository;
        this.eventRepository = eventRepository;
        this.imageCompatService = imageCompatService;
    }

    public RouteStepPlaceDto getPlaceDetailsById(String id) {
        var uuid = parseId(id);
        var place = placeRepository.findById(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found"));

        var imageUrl = place.getMedia().stream()
                .sorted(Comparator.comparingInt(m -> m.getSortOrder() != null ? m.getSortOrder() : Integer.MAX_VALUE))
                .map(m -> m.getUrl())
                .filter(url -> url != null && !url.isBlank())
                .findFirst()
                .orElse(null);
        var images = imageCompatService.fromPrimaryUrl(imageUrl);
        var category = place.getPlaceCategories().stream()
                .map(pc -> pc.getCategory().getSlug())
                .findFirst()
                .orElse(null);

        var address = place.getAddress();
        return new RouteStepPlaceDto(
                place.getId() != null ? place.getId().toString() : null,
                place.getName(),
                place.getName(),
                place.getDescription(),
                address,
                images.imageUrl(),
                images.coverImage(),
                images.previewImage(),
                images.photos(),
                images.images(),
                address,
                place.getLat(),
                place.getLng(),
                category,
                isPlaceOpenNow(place)
        );
    }

    public RouteStepEventDto getEventDetailsById(String id) {
        var uuid = parseId(id);
        var event = eventRepository.findById(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found"));

        var lat = event.getLat();
        var lng = event.getLng();
        var address = event.getAddress();
        String imageUrl = null;
        if (event.getPlace() != null) {
            if (lat == null || lng == null) {
                lat = event.getPlace().getLat();
                lng = event.getPlace().getLng();
            }
            if (address == null || address.isBlank()) {
                address = event.getPlace().getAddress();
            }
            imageUrl = event.getPlace().getMedia().stream()
                    .sorted(Comparator.comparingInt(m -> m.getSortOrder() != null ? m.getSortOrder() : Integer.MAX_VALUE))
                    .map(m -> m.getUrl())
                    .filter(url -> url != null && !url.isBlank())
                    .findFirst()
                    .orElse(null);
        }
        var images = imageCompatService.fromPrimaryUrl(imageUrl);

        return new RouteStepEventDto(
                event.getId() != null ? event.getId().toString() : null,
                event.getName(),
                event.getName(),
                event.getDescription(),
                address,
                images.imageUrl(),
                images.coverImage(),
                images.previewImage(),
                images.photos(),
                images.images(),
                address,
                lat,
                lng,
                toOffsetString(event.getStartTime()),
                toOffsetString(event.getEndTime()),
                isEventOpenNow(event)
        );
    }

    private UUID parseId(String id) {
        try {
            return UUID.fromString(id);
        } catch (Exception exception) {
            throw new InvalidIdFormatException("Invalid id format");
        }
    }

    private String toOffsetString(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toOffsetDateTime().format(ISO_OFFSET);
    }

    private Boolean isEventOpenNow(Event event) {
        var now = LocalDateTime.now();
        if (event.getStartTime() == null) {
            return false;
        }
        if (event.getEndTime() == null) {
            return !event.getStartTime().isAfter(now);
        }
        return !event.getStartTime().isAfter(now) && !event.getEndTime().isBefore(now);
    }

    private Boolean isPlaceOpenNow(Place place) {
        var openingHours = place.getOpeningHours();
        if (openingHours == null || openingHours.isBlank()) {
            return null;
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
            return null;
        }
        var from = LocalTime.parse(matcher.group("from"));
        var to = LocalTime.parse(matcher.group("to"));
        var current = LocalTime.now();
        if (to.isBefore(from)) {
            return !current.isBefore(from) || !current.isAfter(to);
        }
        return !current.isBefore(from) && !current.isAfter(to);
    }
}