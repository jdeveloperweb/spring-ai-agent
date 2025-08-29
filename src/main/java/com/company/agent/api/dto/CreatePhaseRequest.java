package com.company.agent.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePhaseRequest {

    @NotBlank(message = "Nome da fase é obrigatório")
    private String name;

    private String description;

    private String category;

    private Integer orderIndex;
}