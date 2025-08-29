package com.company.agent.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    public enum Status {
        PENDING,
        RUNNING,
        SUCCEEDED,
        FAILED,
        CANCELLED
    }

    public enum Mode {
        AUTONOMOUS,  // Execução automática com tools
        SUPERVISED,  // Requer aprovação para tools
        PLANNING     // Apenas planeja, não executa
    }

    private UUID id;
    private String prompt;
    private Status status;
    private Mode mode;
    private String tenant;
    private UUID flowId;       // Referência ao Flow
    private String result;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    // Business methods
    public void markRunning() {
        this.status = Status.RUNNING;
        this.startedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markSucceeded(String result) {
        this.status = Status.SUCCEEDED;
        this.result = result;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markFailed(String errorMessage) {
        this.status = Status.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = Status.CANCELLED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isCompleted() {
        return status == Status.SUCCEEDED ||
                status == Status.FAILED ||
                status == Status.CANCELLED;
    }

    public boolean canBeExecuted() {
        return status == Status.PENDING;
    }
}
