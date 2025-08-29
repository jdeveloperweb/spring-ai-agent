package com.company.agent.infrastructure.persistence;

import com.company.agent.domain.Task;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Task.Status status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Task.Mode mode;

    @Column(nullable = false, length = 100)
    private String tenant;

    @Column(name = "flow_id")
    private UUID flowId;

    @Column(columnDefinition = "TEXT")
    private String result;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Conversion methods
    public Task toDomain() {
        return Task.builder()
                .id(this.id)
                .prompt(this.prompt)
                .status(this.status)
                .mode(this.mode)
                .tenant(this.tenant)
                .flowId(this.flowId)
                .result(this.result)
                .errorMessage(this.errorMessage)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .startedAt(this.startedAt)
                .completedAt(this.completedAt)
                .build();
    }

    public static TaskEntity fromDomain(Task task) {
        return TaskEntity.builder()
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