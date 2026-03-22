package ru.fshs.tour.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import ru.fshs.tour.controller.dto.auth.AuthResponse;
import ru.fshs.tour.controller.dto.auth.LoginRequest;
import ru.fshs.tour.controller.dto.auth.RefreshTokenRequest;
import ru.fshs.tour.controller.dto.user.UserResponse;
import ru.fshs.tour.security.AppUserPrincipal;
import ru.fshs.tour.security.JwtService;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final CurrentUserService currentUserService;

    public AuthResponse login(LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.login(), request.password())
        );
        var principal = (AppUserPrincipal) authentication.getPrincipal();
        return buildAuthResponse(principal);
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        var username = jwtService.extractUsername(request.refreshToken());
        var principal = (AppUserPrincipal) userDetailsService.loadUserByUsername(username);

        if (!jwtService.isRefreshTokenValid(request.refreshToken(), principal)) {
            throw new org.springframework.security.authentication.BadCredentialsException("Invalid refresh token");
        }

        return buildAuthResponse(principal);
    }

    public UserResponse getCurrentUser() {
        return currentUserService.getCurrentUserResponse();
    }

    private AuthResponse buildAuthResponse(AppUserPrincipal principal) {
        return new AuthResponse(
                jwtService.generateAccessToken(principal),
                jwtService.generateRefreshToken(principal),
                currentUserService.toResponse(principal)
        );
    }
}
