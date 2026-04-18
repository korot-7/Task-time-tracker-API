package com.cdek.timetracker.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(
        @NotBlank(message = "Task title is required")
        @Size(max = 255, message = "Task title must not exceed 255 symbols")
        String title,

        @Size(max = 4000, message = "Task description must not exceed 4000 symbols")
        String description
) {
}
