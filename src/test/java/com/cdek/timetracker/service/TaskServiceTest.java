package com.cdek.timetracker.service;

import com.cdek.timetracker.dto.task.CreateTaskRequest;
import com.cdek.timetracker.dto.task.TaskResponse;
import com.cdek.timetracker.exception.ResourceNotFoundException;
import com.cdek.timetracker.mapper.TaskMapper;
import com.cdek.timetracker.model.Task;
import com.cdek.timetracker.model.TaskStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    @Test
    void createTaskShouldPersistWithNewStatus() {
        doAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(42L);
            return null;
        }).when(taskMapper).insert(any(Task.class));

        TaskResponse response = taskService.createTask(new CreateTaskRequest("Investigate bug", "Find root cause"));

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.status()).isEqualTo(TaskStatus.NEW);
        verify(taskMapper).insert(any(Task.class));
    }

    @Test
    void getTaskByIdShouldThrowWhenNotFound() {
        when(taskMapper.findById(99L)).thenReturn(null);

        assertThatThrownBy(() -> taskService.getTaskById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found");
    }

    @Test
    void updateStatusShouldThrowWhenTaskDoesNotExist() {
        when(taskMapper.updateStatus(eq(15L), eq(TaskStatus.DONE), any())).thenReturn(0);

        assertThatThrownBy(() -> taskService.updateTaskStatus(15L, TaskStatus.DONE))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found");
    }
}
