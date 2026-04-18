package com.cdek.timetracker.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 64, message = "Username length must be between 3 and 64 symbols")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password length must be between 8 and 72 symbols")
        String password
) {
}
