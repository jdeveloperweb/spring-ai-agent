package com.company.agent.api.dto;

import com.company.agent.domain.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateTaskRequest {

    @NotBlank(message = "Prompt é obrigatório")
    private String prompt;

    @NotNull(message = "Mode é obrigatório")
    private Task.Mode mode = Task.Mode.AUTONOMOUS;

    private UUID flowId;

    private boolean sync = false; // Execução síncrona por padrão
}
