package com.company.agent.api.dto;

import com.company.agent.domain.Task;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TaskDto {

    private UUID id;
    private String prompt;
    private Task.Status status;
    private Task.Mode mode;
    private String tenant;
    private UUID flowId;
    private String result;
    private String errorMessage;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime completedAt;

    public static TaskDto fromDomain(Task task) {
        return TaskDto.builder()
                .id(task.getId())
                .prompt(task.getPrompt())
                .status(task.getStatus())
                .mode(task.getMode())
                .tenant(task.getTenant())
                .flowId(task.getFlowId())
                .result(task.getResult())
                .errorMessage(task.getErrorMessage())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .startedAt(task.getStartedAt())
                .completedAt(task.getCompletedAt())
                .build();
    }
}
