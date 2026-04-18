package com.cdek.timetracker.mapper;

import com.cdek.timetracker.model.AppUser;
import com.cdek.timetracker.model.Task;
import com.cdek.timetracker.model.TaskStatus;
import com.cdek.timetracker.model.TimeRecord;
import com.cdek.timetracker.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class MapperIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("timetracker_test")
            .withUsername("test_user")
            .withPassword("test_password");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TimeRecordMapper timeRecordMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE time_records, tasks, users RESTART IDENTITY CASCADE");
    }

    @Test
    void shouldPersistAndReadTaskAndTimeRecords() {
        AppUser user = new AppUser();
        user.setUsername("dao-user");
        user.setPasswordHash("hash");
        user.setRole(UserRole.USER);
        userMapper.insert(user);

        Task task = new Task();
        task.setTitle("Integration test task");
        task.setDescription("DAO checks");
        task.setStatus(TaskStatus.NEW);
        task.setCreatedAt(LocalDateTime.parse("2026-01-01T10:00:00"));
        task.setUpdatedAt(LocalDateTime.parse("2026-01-01T10:00:00"));
        taskMapper.insert(task);

        TimeRecord timeRecord = new TimeRecord();
        timeRecord.setEmployeeId(user.getId());
        timeRecord.setTaskId(task.getId());
        timeRecord.setStartedAt(LocalDateTime.parse("2026-01-01T11:00:00"));
        timeRecord.setEndedAt(LocalDateTime.parse("2026-01-01T12:00:00"));
        timeRecord.setWorkDescription("DAO integration");
        timeRecord.setCreatedAt(LocalDateTime.parse("2026-01-01T12:01:00"));
        timeRecordMapper.insert(timeRecord);

        Task storedTask = taskMapper.findById(task.getId());
        List<TimeRecord> records = timeRecordMapper.findByEmployeeAndPeriod(
                user.getId(),
                LocalDateTime.parse("2026-01-01T00:00:00"),
                LocalDateTime.parse("2026-01-02T00:00:00")
        );

        assertThat(storedTask).isNotNull();
        assertThat(storedTask.getTitle()).isEqualTo("Integration test task");
        assertThat(records).hasSize(1);
        assertThat(records.get(0).getTaskId()).isEqualTo(task.getId());
        assertThat(records.get(0).getEmployeeId()).isEqualTo(user.getId());
    }
}
