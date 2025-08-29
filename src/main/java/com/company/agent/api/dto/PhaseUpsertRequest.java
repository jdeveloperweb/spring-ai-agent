package com.company.agent.api.dto;

import lombok.Data;

import java.util.Map;

@Data
public class PhaseUpsertRequest {

    private Map<String, Object> variables;
    private Map<String, Object> toolPolicy;
    private String ragFilter;
    private String systemPromptTemplate;
}
