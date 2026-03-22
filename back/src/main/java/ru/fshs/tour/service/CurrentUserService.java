package ru.fshs.tour.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.fshs.tour.controller.dto.user.UserResponse;
import ru.fshs.tour.security.AppUserPrincipal;

@Service
public class CurrentUserService {

    public AppUserPrincipal getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
            throw new BadCredentialsException("Authenticated user not found");
        }
        return principal;
    }

    public UserResponse getCurrentUserResponse() {
        return toResponse(getCurrentUser());
    }

    public UserResponse toResponse(AppUserPrincipal principal) {
        return new UserResponse(principal.getId(), principal.getUsername(), principal.getName(), principal.getUserType());
    }
}
