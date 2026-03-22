package ru.fshs.tour.assistant.domain.place;

import java.util.UUID;
import ru.fshs.tour.assistant.domain.common.AssistantSystemResponse;

/**
 * Stub business facade for place-related assistant capabilities.
 */
public interface PlaceAssistantService {

    AssistantSystemResponse search(PlaceSearchCriteria criteria);

    AssistantSystemResponse explain(UUID placeId);

    AssistantSystemResponse nearbySuggestions(Double lat, Double lng, double radiusKm);
}
