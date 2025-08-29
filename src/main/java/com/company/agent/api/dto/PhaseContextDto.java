package com.company.agent.api.dto;

import com.company.agent.domain.PhaseContext;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class PhaseContextDto {

    private UUID id;
    private UUID flowId;
    private String phaseName;
    private Map<String, Object> variables;
    private Map<String, Object> toolPolicy;
    private String ragFilter;
    private String systemPromptTemplate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static PhaseContextDto fromDomain(PhaseContext context) {
        return PhaseContextDto.builder()
                .id(context.getId())
                .flowId(context.getFlowId())
                .phaseName(context.getPhaseName())
                .variables(context.getVariables())
                .toolPolicy(context.getToolPolicy())
                .ragFilter(context.getRagFilter())
                .systemPromptTemplate(context.getSystemPromptTemplate())
                .createdAt(context.getCreatedAt())
                .updatedAt(context.getUpdatedAt())
                .build();
    }
}
