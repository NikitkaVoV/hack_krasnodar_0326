package ru.fshs.tour.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
        Jwt jwt,
        BootstrapUser bootstrapUser,
        List<String> publicUrls
) {

    public record Jwt(
            String secret,
            long accessExpirationMs,
            long refreshExpirationMs,
            String issuer
    ) {
    }

    public record BootstrapUser(
            String login,
            String password
    ) {
    }
}
