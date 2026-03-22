package ru.fshs.tour.controller.dto.user;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String login,
        String name,
        String userType
) {
}
