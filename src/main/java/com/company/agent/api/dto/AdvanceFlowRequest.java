package com.company.agent.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class AdvanceFlowRequest {

    @NotBlank(message = "Fase destino é obrigatória")
    private String to;

    private Map<String, Object> guardVars;
}
