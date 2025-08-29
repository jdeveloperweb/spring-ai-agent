package com.company.agent.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhaseContext {

    private UUID id;
    private UUID flowId;
    private String phaseName;
    private Map<String, Object> variables;           // Variáveis do contexto
    private Map<String, Object> toolPolicy;         // Política de tools (allow/deny/domains)
    private String ragFilter;                       // Filtro para RAG
    private String systemPromptTemplate;            // Template do prompt do sistema
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Business methods
    public String getVariable(String key) {
        return variables != null ? (String) variables.get(key) : null;
    }

    public void setVariable(String key, Object value) {
        if (variables == null) {
            variables = new java.util.HashMap<>();
        }
        variables.put(key, value);
        this.updatedAt = LocalDateTime.now();
    }

    public boolean hasToolPolicy() {
        return toolPolicy != null && !toolPolicy.isEmpty();
    }

    public boolean hasRagFilter() {
        return ragFilter != null && !ragFilter.trim().isEmpty();
    }

    public boolean hasSystemPromptTemplate() {
        return systemPromptTemplate != null && !systemPromptTemplate.trim().isEmpty();
    }
}
