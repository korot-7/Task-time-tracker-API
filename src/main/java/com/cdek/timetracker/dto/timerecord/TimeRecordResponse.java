package com.cdek.timetracker.dto.timerecord;

import java.time.LocalDateTime;

public record TimeRecordResponse(
        Long id,
        Long employeeId,
        Long taskId,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        String workDescription,
        long durationMinutes
) {
}
