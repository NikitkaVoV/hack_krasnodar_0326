package ru.fshs.tour.controller.impl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.fshs.tour.controller.dto.user.UserResponse;
import ru.fshs.tour.service.CurrentUserService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Demo", description = "Public and protected demo endpoints")
public class DemoController {

    private final CurrentUserService currentUserService;

    @GetMapping("/public/ping")
    @Operation(summary = "Public endpoint", security = {})
    @ApiResponse(responseCode = "200", description = "Public access confirmed")
    public Map<String, String> publicPing() {
        return Map.of("message", "public ok");
    }

    @GetMapping("/private/ping")
    @Operation(
            summary = "Protected endpoint",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Authorized access confirmed")
    public Map<String, String> privatePing() {
        return Map.of("message", "private ok");
    }

    @GetMapping("/private/user")
    @Operation(
            summary = "Protected endpoint returning current user from security context",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Current user returned")
    public UserResponse currentUser() {
        return currentUserService.getCurrentUserResponse();
    }
}
