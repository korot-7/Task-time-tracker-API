package com.cdek.timetracker.dto.timerecord;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateTimeRecordRequest(
        Long employeeId,

        @NotNull(message = "Task id is required")
        Long taskId,

        @NotNull(message = "Start datetime is required")
        LocalDateTime startedAt,

        @NotNull(message = "End datetime is required")
        LocalDateTime endedAt,

        @Size(max = 4000, message = "Work description must not exceed 4000 symbols")
        String workDescription
) {
}
