package com.company.agent.infrastructure.persistence;

import com.company.agent.domain.Phase;
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
@Table(name = "phases", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenant", "name"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 100)
    private String tenant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Phase.Status status;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(length = 50)
    private String category;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Conversion methods
    public Phase toDomain() {
        return Phase.builder()
                .id(this.id)
                .name(this.name)
                .description(this.description)
                .tenant(this.tenant)
                .status(this.status)
                .orderIndex(this.orderIndex)
                .category(this.category)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    public static PhaseEntity fromDomain(Phase phase) {
        return PhaseEntity.builder()
                .id(phase.getId())
                .name(phase.getName())
                .description(phase.getDescription())
                .tenant(phase.getTenant())
                .status(phase.getStatus())
                .orderIndex(phase.getOrderIndex())
                .category(phase.getCategory())
                .createdAt(phase.getCreatedAt())
                .updatedAt(phase.getUpdatedAt())
                .build();
    }
}
