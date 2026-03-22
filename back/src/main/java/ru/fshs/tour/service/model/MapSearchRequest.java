package ru.fshs.tour.service.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record MapSearchRequest(
        double lat,
        double lng,
        double radiusKm,
        String types,
        String categories,
        Integer minBudget,
        Integer maxBudget,
        String duration,
        LocalDate date,
        LocalTime time,
        Boolean openNow,
        String suitableFor,
        String sortBy
) {
}
