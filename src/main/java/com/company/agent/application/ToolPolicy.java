package com.company.agent.application;

import com.company.agent.domain.PhaseContext;
import com.company.agent.infrastructure.tools.HttpTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public class ToolPolicy {

    public ChatClient applyPolicy(ChatClient baseChatClient, PhaseContext phaseContext) {
        if (phaseContext == null || !phaseContext.hasToolPolicy()) {
            log.debug("Nenhuma política de tools definida, usando configuração padrão");
            return baseChatClient;
        }

        Map<String, Object> policy = phaseContext.getToolPolicy();

        return baseChatClient.mutate()
                .tools(tools -> filterTools(tools, policy))
                .build();
    }

    private List<Function<?, ?>> filterTools(List<Function<?, ?>> originalTools, Map<String, Object> policy) {
        Set<String> allowedTools = extractStringSet(policy, "allow");
        Set<String> deniedTools = extractStringSet(policy, "deny");
        Set<String> allowedDomains = extractStringSet(policy, "allowDomains");

        log.debug("Aplicando política: allow={}, deny={}, domains={}",
                allowedTools, deniedTools, allowedDomains);

        return originalTools.stream()
                .filter(tool -> isToolAllowed(tool, allowedTools, deniedTools))
                .map(tool -> configureToolDomains(tool, allowedDomains))
                .toList();
    }

    private boolean isToolAllowed(Function<?, ?> tool, Set<String> allowedTools, Set<String> deniedTools) {
        String toolName = getToolName(tool);

        // Se há lista de permitidos, só permite os listados
        if (!allowedTools.isEmpty() && !allowedTools.contains(toolName)) {
            log.debug("Tool {} não está na lista de permitidos", toolName);
            return false;
        }

        // Se está na lista de negados, não permite
        if (deniedTools.contains(toolName)) {
            log.debug("Tool {} está na lista de negados", toolName);
            return false;
        }

        return true;
    }

    private Function<?, ?> configureToolDomains(Function<?, ?> tool, Set<String> allowedDomains) {
        if (allowedDomains.isEmpty()) {
            return tool;
        }

        // Para HttpTool, configurar domínios permitidos
        if (tool instanceof HttpTool httpTool) {
            return httpTool.withAllowedDomains(allowedDomains);
        }

        return tool;
    }

    private String getToolName(Function<?, ?> tool) {
        // Extrair nome da tool baseado na classe ou annotation
        String className = tool.getClass().getSimpleName();

        if (className.endsWith("Tool")) {
            return className.substring(0, className.length() - 4).toLowerCase();
        }

        return className.toLowerCase();
    }

    @SuppressWarnings("unchecked")
    private Set<String> extractStringSet(Map<String, Object> policy, String key) {
        Object value = policy.get(key);

        if (value == null) {
            return Set.of();
        }

        if (value instanceof List<?> list) {
            return new HashSet<>(
                    list.stream()
                            .filter(Objects::nonNull)
                            .map(Object::toString)
                            .toList()
            );
        }

        if (value instanceof String str) {
            return Set.of(str);
        }

        log.warn("Valor inválido para chave '{}' na política: {}", key, value);
        return Set.of();
    }
}
