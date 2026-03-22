package ru.fshs.tour.controller.dto.auth;

import ru.fshs.tour.controller.dto.user.UserResponse;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserResponse user
) {
}
