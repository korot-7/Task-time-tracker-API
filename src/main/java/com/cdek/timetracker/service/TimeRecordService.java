package com.cdek.timetracker.service;

import com.cdek.timetracker.dto.timerecord.CreateTimeRecordRequest;
import com.cdek.timetracker.dto.timerecord.TimeRecordListResponse;
import com.cdek.timetracker.dto.timerecord.TimeRecordResponse;
import com.cdek.timetracker.exception.BadRequestException;
import com.cdek.timetracker.exception.ForbiddenOperationException;
import com.cdek.timetracker.exception.ResourceNotFoundException;
import com.cdek.timetracker.mapper.TaskMapper;
import com.cdek.timetracker.mapper.TimeRecordMapper;
import com.cdek.timetracker.mapper.UserMapper;
import com.cdek.timetracker.model.TimeRecord;
import com.cdek.timetracker.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TimeRecordService {
    private final TimeRecordMapper timeRecordMapper;
    private final TaskMapper taskMapper;
    private final UserMapper userMapper;

    public TimeRecordService(TimeRecordMapper timeRecordMapper, TaskMapper taskMapper, UserMapper userMapper) {
        this.timeRecordMapper = timeRecordMapper;
        this.taskMapper = taskMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    public TimeRecordResponse createTimeRecord(CreateTimeRecordRequest request, UserPrincipal actor) {
        validateRange(request.startedAt(), request.endedAt());

        if (!taskMapper.existsById(request.taskId())) {
            throw new ResourceNotFoundException("Task not found: " + request.taskId());
        }

        Long employeeId = resolveEmployeeId(actor, request.employeeId());
        if (userMapper.findById(employeeId) == null) {
            throw new ResourceNotFoundException("Employee not found: " + employeeId);
        }

        TimeRecord timeRecord = new TimeRecord();
        timeRecord.setEmployeeId(employeeId);
        timeRecord.setTaskId(request.taskId());
        timeRecord.setStartedAt(request.startedAt());
        timeRecord.setEndedAt(request.endedAt());
        timeRecord.setWorkDescription(request.workDescription());
        timeRecord.setCreatedAt(LocalDateTime.now());
        timeRecordMapper.insert(timeRecord);

        return mapToResponse(timeRecord);
    }

    @Transactional(readOnly = true)
    public TimeRecordListResponse getByEmployeeAndPeriod(
            Long employeeId,
            LocalDateTime from,
            LocalDateTime to,
            UserPrincipal actor
    ) {
        validatePeriod(from, to);
        enforceAccess(employeeId, actor);

        if (userMapper.findById(employeeId) == null) {
            throw new ResourceNotFoundException("Employee not found: " + employeeId);
        }

        List<TimeRecordResponse> responses = timeRecordMapper.findByEmployeeAndPeriod(employeeId, from, to)
                .stream()
                .map(this::mapToResponse)
                .toList();

        long totalDurationMinutes = responses.stream()
                .mapToLong(TimeRecordResponse::durationMinutes)
                .sum();

        return new TimeRecordListResponse(responses, totalDurationMinutes);
    }

    private void validateRange(LocalDateTime startedAt, LocalDateTime endedAt) {
        if (!endedAt.isAfter(startedAt)) {
            throw new BadRequestException("End datetime must be after start datetime");
        }
    }

    private void validatePeriod(LocalDateTime from, LocalDateTime to) {
        if (to.isBefore(from)) {
            throw new BadRequestException("The 'to' datetime must not be before 'from' datetime");
        }
    }

    private Long resolveEmployeeId(UserPrincipal actor, Long requestedEmployeeId) {
        if (requestedEmployeeId == null) {
            return actor.getId();
        }

        if (!actor.isAdmin() && !actor.getId().equals(requestedEmployeeId)) {
            throw new ForbiddenOperationException("You can only create time records for your own employee id");
        }
        return requestedEmployeeId;
    }

    private void enforceAccess(Long employeeId, UserPrincipal actor) {
        if (!actor.isAdmin() && !actor.getId().equals(employeeId)) {
            throw new ForbiddenOperationException("You can only access your own time records");
        }
    }

    private TimeRecordResponse mapToResponse(TimeRecord timeRecord) {
        long durationMinutes = Duration.between(timeRecord.getStartedAt(), timeRecord.getEndedAt()).toMinutes();
        return new TimeRecordResponse(
                timeRecord.getId(),
                timeRecord.getEmployeeId(),
                timeRecord.getTaskId(),
                timeRecord.getStartedAt(),
                timeRecord.getEndedAt(),
                timeRecord.getWorkDescription(),
                durationMinutes
        );
    }
}
