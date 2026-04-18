package com.cdek.timetracker.dto.task;

import com.cdek.timetracker.model.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequest(
        @NotNull(message = "Task status is required")
        TaskStatus status
) {
}
