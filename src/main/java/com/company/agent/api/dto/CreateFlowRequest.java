package com.company.agent.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateFlowRequest {

    @NotBlank(message = "Nome do flow é obrigatório")
    private String name;

    private String description;

    @NotBlank(message = "Fase inicial é obrigatória")
    private String initialPhase = "DISCOVERY";
}
