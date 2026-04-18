package com.cdek.timetracker.service;

import com.cdek.timetracker.dto.task.CreateTaskRequest;
import com.cdek.timetracker.dto.task.TaskResponse;
import com.cdek.timetracker.exception.ResourceNotFoundException;
import com.cdek.timetracker.mapper.TaskMapper;
import com.cdek.timetracker.model.Task;
import com.cdek.timetracker.model.TaskStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TaskService {
    private final TaskMapper taskMapper;

    public TaskService(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Task task = new Task();
        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        task.setStatus(TaskStatus.NEW);
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        taskMapper.insert(task);
        return mapToResponse(task);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        Task task = taskMapper.findById(id);
        if (task == null) {
            throw new ResourceNotFoundException("Task not found: " + id);
        }
        return mapToResponse(task);
    }

    @Transactional
    public TaskResponse updateTaskStatus(Long id, TaskStatus status) {
        int updatedRows = taskMapper.updateStatus(id, status, LocalDateTime.now());
        if (updatedRows == 0) {
            throw new ResourceNotFoundException("Task not found: " + id);
        }
        Task updatedTask = taskMapper.findById(id);
        return mapToResponse(updatedTask);
    }

    private TaskResponse mapToResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
