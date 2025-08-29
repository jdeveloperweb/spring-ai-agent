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
public class Flow {

    private UUID id;
    private String name;
    private String description;
    private String tenant;
    private String currentPhase;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Business methods
    public void advanceToPhase(String newPhase) {
        this.currentPhase = newPhase;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean canAdvanceTo(String targetPhase) {
        // Implementar lógica de guards/validações de transição
        // Por enquanto, permite qualquer transição
        return targetPhase != null && !targetPhase.equals(this.currentPhase);
    }
}
