package com.cdek.timetracker.dto.auth;

import com.cdek.timetracker.model.UserRole;

public record UserResponse(
        Long id,
        String username,
        UserRole role
) {
}
