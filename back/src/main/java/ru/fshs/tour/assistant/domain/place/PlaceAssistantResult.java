package ru.fshs.tour.assistant.domain.place;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record PlaceAssistantResult(
        String note,
        List<PlaceCard> items
) {

    public PlaceAssistantResult {
        if (items == null) {
            items = new ArrayList<>();
        }
    }

    public record PlaceCard(
            UUID id,
            String name,
            String shortDescription,
            String address,
            Double lat,
            Double lng,
            Double distanceKm,
            List<String> categories,
            List<String> tags,
            List<String> constraints,
            Integer popularity,
            String status
    ) {
    }
}
