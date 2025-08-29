package com.company.agent.infrastructure.persistence;

import com.company.agent.domain.PhaseContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "phase_contexts", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"flow_id", "phase_name"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhaseContextEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "flow_id", nullable = false)
    private UUID flowId;

    @Column(name = "phase_name", nullable = false, length = 100)
    private String phaseName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "variables", columnDefinition = "jsonb")
    private Map<String, Object> variables;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tool_policy", columnDefinition = "jsonb")
    private Map<String, Object> toolPolicy;

    @Column(name = "rag_filter", columnDefinition = "TEXT")
    private String ragFilter;

    @Column(name = "system_prompt_template", columnDefinition = "TEXT")
    private String systemPromptTemplate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Conversion methods
    public PhaseContext toDomain() {
        return PhaseContext.builder()
                .id(this.id)
                .flowId(this.flowId)
                .phaseName(this.phaseName)
                .variables(this.variables)
                .toolPolicy(this.toolPolicy)
                .ragFilter(this.ragFilter)
                .systemPromptTemplate(this.systemPromptTemplate)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    public static PhaseContextEntity fromDomain(PhaseContext phaseContext) {
        return PhaseContextEntity.builder()
                .id(phaseContext.getId())
                .flowId(phaseContext.getFlowId())
                .phaseName(phaseContext.getPhaseName())
                .variables(phaseContext.getVariables())
                .toolPolicy(phaseContext.getToolPolicy())
                .ragFilter(phaseContext.getRagFilter())
                .systemPromptTemplate(phaseContext.getSystemPromptTemplate())
                .createdAt(phaseContext.getCreatedAt())
                .updatedAt(phaseContext.getUpdatedAt())
                .build();
    }
}
