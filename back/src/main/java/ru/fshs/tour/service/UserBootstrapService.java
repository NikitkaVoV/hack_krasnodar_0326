package ru.fshs.tour.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.fshs.tour.config.SecurityProperties;
import ru.fshs.tour.domain.user.User;
import ru.fshs.tour.repository.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserBootstrapService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties securityProperties;

    @Override
    public void run(String... args) {
        var bootstrapUser = securityProperties.bootstrapUser();
        if (bootstrapUser == null || isBlank(bootstrapUser.login()) || isBlank(bootstrapUser.password())) {
            log.warn("Bootstrap user is not configured");
            return;
        }

        userRepository.findByLogin(bootstrapUser.login()).orElseGet(() -> {
            var user = userRepository.save(User.builder()
                    .login(bootstrapUser.login())
                    .passwordHash(passwordEncoder.encode(bootstrapUser.password()))
                    .name(bootstrapUser.login())
                    .build());
            log.info("Bootstrap user created with login={}", user.getLogin());
            return user;
        });
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
