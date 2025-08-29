package com.company.agent.api.dto;

import lombok.Data;

@Data
public class UpdatePhaseRequest {

    private String description;

    private String category;

    private Integer orderIndex;
}