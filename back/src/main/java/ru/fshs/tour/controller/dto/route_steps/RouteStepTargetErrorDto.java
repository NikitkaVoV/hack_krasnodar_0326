package ru.fshs.tour.controller.dto.route_steps;

public record RouteStepTargetErrorDto(
        String message,
        String code,
        ErrorBody error
) {
    public record ErrorBody(
            String code,
            String message,
            Object details
    ) {
    }
}
