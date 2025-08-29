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
public class Phase {

    public enum Status {
        ACTIVE,
        INACTIVE,
        DEPRECATED
    }

    private UUID id;
    private String name;
    private String description;
    private String tenant;
    private Status status;
    private Integer orderIndex;  // Ordem da fase no fluxo
    private String category;     // Categoria/tipo da fase
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Business methods
    public void activate() {
        this.status = Status.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.status = Status.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void deprecate() {
        this.status = Status.DEPRECATED;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return Status.ACTIVE.equals(this.status);
    }

    public boolean canBeUsed() {
        return Status.ACTIVE.equals(this.status);
    }
}
