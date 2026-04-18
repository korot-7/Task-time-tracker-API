package com.cdek.timetracker.controller;

import com.cdek.timetracker.dto.timerecord.CreateTimeRecordRequest;
import com.cdek.timetracker.dto.timerecord.TimeRecordListResponse;
import com.cdek.timetracker.dto.timerecord.TimeRecordResponse;
import com.cdek.timetracker.security.UserPrincipal;
import com.cdek.timetracker.service.TimeRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/time-records")
@Tag(name = "Time records", description = "Employee time tracking endpoints")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class TimeRecordController {
    private final TimeRecordService timeRecordService;

    public TimeRecordController(TimeRecordService timeRecordService) {
        this.timeRecordService = timeRecordService;
    }

    @Operation(summary = "Create time record")
    @PostMapping
    public TimeRecordResponse create(
            @Valid @RequestBody CreateTimeRecordRequest request,
            @AuthenticationPrincipal UserPrincipal actor
    ) {
        return timeRecordService.createTimeRecord(request, actor);
    }

    @Operation(summary = "Get employee time records in period")
    @GetMapping
    public TimeRecordListResponse getByEmployeeAndPeriod(
            @RequestParam @NotNull(message = "Employee id is required") Long employeeId,
            @RequestParam @NotNull(message = "From datetime is required")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @NotNull(message = "To datetime is required")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @AuthenticationPrincipal UserPrincipal actor
    ) {
        return timeRecordService.getByEmployeeAndPeriod(employeeId, from, to, actor);
    }
}
