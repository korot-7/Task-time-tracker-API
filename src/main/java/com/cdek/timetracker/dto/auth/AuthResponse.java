package com.cdek.timetracker.dto.auth;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds
) {
}
