package com.company.agent.infrastructure.persistence;

import com.company.agent.domain.Flow;
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
@Table(name = "flows")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 100)
    private String tenant;

    @Column(name = "current_phase", nullable = false, length = 100)
    private String currentPhase;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Conversion methods
    public Flow toDomain() {
        return Flow.builder()
                .id(this.id)
                .name(this.name)
                .description(this.description)
                .tenant(this.tenant)
                .currentPhase(this.currentPhase)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    public static FlowEntity fromDomain(Flow flow) {
        return FlowEntity.builder()
                .id(flow.getId())
                .name(flow.getName())
                .description(flow.getDescription())
                .tenant(flow.getTenant())
                .currentPhase(flow.getCurrentPhase())
                .createdAt(flow.getCreatedAt())
                .updatedAt(flow.getUpdatedAt())
                .build();
    }
}
