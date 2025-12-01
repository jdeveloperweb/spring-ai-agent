package com.company.agent.api;

import com.company.agent.api.dto.CreateTaskRequest;
import com.company.agent.api.dto.TaskDto;
import com.company.agent.application.TaskService;
import com.company.agent.domain.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody CreateTaskRequest request) {
        log.info("Criando task: prompt='{}', mode={}, flowId={}, sync={}",
                request.getPrompt().substring(0, Math.min(50, request.getPrompt().length())),
                request.getMode(), request.getFlowId(), request.isSync());

        Task task = taskService.createAndExecuteTask(
                request.getPrompt(),
                request.getMode(),
                request.getFlowId(),
                request.isSync()
        );

        TaskDto response = TaskDto.fromDomain(task);

        HttpStatus status = task.getStatus() == Task.Status.PENDING ?
                HttpStatus.ACCEPTED : HttpStatus.OK;

        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTask(@PathVariable UUID id) {
        Task task = taskService.getTask(id);
        return ResponseEntity.ok(TaskDto.fromDomain(task));
    }

    @GetMapping
    public ResponseEntity<List<TaskDto>> listTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID flowId) {

        List<Task> tasks;

        if (flowId != null) {
            tasks = taskService.listTasksByFlow(flowId);
        } else if (status != null) {
            Task.Status taskStatus = Task.Status.valueOf(status.toUpperCase());
            tasks = taskService.listTasksByStatus(taskStatus);
        } else {
            tasks = taskService.listTasks();
        }

        List<TaskDto> response = tasks.stream()
                .map(TaskDto::fromDomain)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<TaskDto> executeTask(@PathVariable UUID id) {
        Task task = taskService.executeTask(id);
        return ResponseEntity.ok(TaskDto.fromDomain(task));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelTask(@PathVariable UUID id) {
        taskService.cancelTask(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));
    }
}
