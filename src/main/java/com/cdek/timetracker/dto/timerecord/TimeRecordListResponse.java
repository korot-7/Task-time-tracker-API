package com.cdek.timetracker.dto.timerecord;

import java.util.List;

public record TimeRecordListResponse(
        List<TimeRecordResponse> records,
        long totalDurationMinutes
) {
}
