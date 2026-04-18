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
import com.cdek.timetracker.model.AppUser;
import com.cdek.timetracker.model.TimeRecord;
import com.cdek.timetracker.model.UserRole;
import com.cdek.timetracker.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeRecordServiceTest {

    @Mock
    private TimeRecordMapper timeRecordMapper;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private TimeRecordService timeRecordService;

    @Test
    void createTimeRecordShouldFailWhenEndBeforeStart() {
        UserPrincipal actor = new UserPrincipal(1L, "user", "hash", UserRole.USER);
        CreateTimeRecordRequest request = new CreateTimeRecordRequest(
                null,
                10L,
                LocalDateTime.parse("2026-01-10T11:00:00"),
                LocalDateTime.parse("2026-01-10T10:00:00"),
                "Work"
        );

        assertThatThrownBy(() -> timeRecordService.createTimeRecord(request, actor))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("End datetime must be after start datetime");
    }

    @Test
    void createTimeRecordShouldFailWhenTaskDoesNotExist() {
        UserPrincipal actor = new UserPrincipal(1L, "user", "hash", UserRole.USER);
        CreateTimeRecordRequest request = new CreateTimeRecordRequest(
                null,
                100L,
                LocalDateTime.parse("2026-01-10T09:00:00"),
                LocalDateTime.parse("2026-01-10T10:00:00"),
                "Work"
        );
        when(taskMapper.existsById(100L)).thenReturn(false);

        assertThatThrownBy(() -> timeRecordService.createTimeRecord(request, actor))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found");
    }

    @Test
    void createTimeRecordShouldFailForForeignEmployeeWhenActorIsNotAdmin() {
        UserPrincipal actor = new UserPrincipal(1L, "user", "hash", UserRole.USER);
        CreateTimeRecordRequest request = new CreateTimeRecordRequest(
                2L,
                10L,
                LocalDateTime.parse("2026-01-10T09:00:00"),
                LocalDateTime.parse("2026-01-10T10:00:00"),
                "Work"
        );
        when(taskMapper.existsById(10L)).thenReturn(true);

        assertThatThrownBy(() -> timeRecordService.createTimeRecord(request, actor))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessageContaining("own employee id");
    }

    @Test
    void getByEmployeeAndPeriodShouldFailForForeignEmployeeWhenActorIsNotAdmin() {
        UserPrincipal actor = new UserPrincipal(1L, "user", "hash", UserRole.USER);

        assertThatThrownBy(() -> timeRecordService.getByEmployeeAndPeriod(
                2L,
                LocalDateTime.parse("2026-01-01T00:00:00"),
                LocalDateTime.parse("2026-01-02T00:00:00"),
                actor
        ))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessageContaining("own time records");
    }

    @Test
    void getByEmployeeAndPeriodShouldReturnAggregatedDuration() {
        UserPrincipal actor = new UserPrincipal(1L, "user", "hash", UserRole.USER);
        AppUser appUser = new AppUser();
        appUser.setId(1L);

        TimeRecord first = new TimeRecord();
        first.setId(10L);
        first.setEmployeeId(1L);
        first.setTaskId(3L);
        first.setStartedAt(LocalDateTime.parse("2026-01-01T10:00:00"));
        first.setEndedAt(LocalDateTime.parse("2026-01-01T11:30:00"));
        first.setWorkDescription("First");

        TimeRecord second = new TimeRecord();
        second.setId(11L);
        second.setEmployeeId(1L);
        second.setTaskId(3L);
        second.setStartedAt(LocalDateTime.parse("2026-01-01T12:00:00"));
        second.setEndedAt(LocalDateTime.parse("2026-01-01T12:45:00"));
        second.setWorkDescription("Second");

        when(userMapper.findById(1L)).thenReturn(appUser);
        when(timeRecordMapper.findByEmployeeAndPeriod(any(), any(), any())).thenReturn(List.of(first, second));

        TimeRecordListResponse response = timeRecordService.getByEmployeeAndPeriod(
                1L,
                LocalDateTime.parse("2026-01-01T00:00:00"),
                LocalDateTime.parse("2026-01-02T00:00:00"),
                actor
        );

        assertThat(response.totalDurationMinutes()).isEqualTo(135L);
        assertThat(response.records()).hasSize(2);
    }

    @Test
    void createTimeRecordShouldUseActorIdWhenEmployeeIdIsEmpty() {
        UserPrincipal actor = new UserPrincipal(5L, "user", "hash", UserRole.USER);
        AppUser appUser = new AppUser();
        appUser.setId(5L);
        when(taskMapper.existsById(10L)).thenReturn(true);
        when(userMapper.findById(5L)).thenReturn(appUser);
        doAnswer(invocation -> {
            TimeRecord record = invocation.getArgument(0);
            record.setId(55L);
            return null;
        }).when(timeRecordMapper).insert(any(TimeRecord.class));

        TimeRecordResponse response = timeRecordService.createTimeRecord(
                new CreateTimeRecordRequest(
                        null,
                        10L,
                        LocalDateTime.parse("2026-01-10T09:00:00"),
                        LocalDateTime.parse("2026-01-10T10:15:00"),
                        "Done"
                ),
                actor
        );

        assertThat(response.id()).isEqualTo(55L);
        assertThat(response.employeeId()).isEqualTo(5L);
        assertThat(response.durationMinutes()).isEqualTo(75L);
    }
}
