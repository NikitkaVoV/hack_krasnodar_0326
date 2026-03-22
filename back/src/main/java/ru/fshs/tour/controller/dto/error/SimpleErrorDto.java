package ru.fshs.tour.controller.dto.error;

public record SimpleErrorDto(
        String message,
        String code
) {
}
