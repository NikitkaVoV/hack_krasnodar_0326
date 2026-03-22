package ru.fshs.tour.controller.dto.routes;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record AiRouteSaveRequest(
        @NotNull @Valid AiRouteDraftDto route
) {
}
