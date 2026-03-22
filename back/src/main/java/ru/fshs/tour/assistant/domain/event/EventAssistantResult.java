package ru.fshs.tour.assistant.domain.event;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record EventAssistantResult(
        String note,
        List<EventCard> items
) {

    public EventAssistantResult {
        if (items == null) {
            items = new ArrayList<>();
        }
    }

    public record EventCard(
            UUID id,
            String name,
            String description,
            String address,
            Double lat,
            Double lng,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String placeName,
            List<String> categories,
            List<String> tags,
            List<String> constraints,
            Integer popularity,
            String status
    ) {
    }
}