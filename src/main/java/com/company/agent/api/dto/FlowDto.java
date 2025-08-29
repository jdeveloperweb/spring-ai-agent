package com.company.agent.api.dto;

import com.company.agent.domain.Flow;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class FlowDto {

    private UUID id;
    private String name;
    private String description;
    private String tenant;
    private String currentPhase;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static FlowDto fromDomain(Flow flow) {
        return FlowDto.builder()
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
