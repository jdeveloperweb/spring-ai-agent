package com.company.agent.application;

import com.company.agent.domain.*;
import com.company.agent.infrastructure.ai.RagAdvisorConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentOrchestrator {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final TaskRepository taskRepository;
    private final FlowRepository flowRepository;
    private final PhaseContextRepository phaseContextRepository;
    private final TemplateEngine templateEngine;
    private final ToolPolicy toolPolicy;
    private final RagAdvisorConfig ragAdvisorConfig;

    @Value("${agent.rag.similarity-threshold:0.75}")
    private double similarityThreshold;

    @Value("${agent.rag.top-k:6}")
    private int topK;

    @Transactional
    public Task run(Task task) {
        log.info("Executando task {} para tenant {}", task.getId(), task.getTenant());

        try {
            // Marcar como executando
            task.markRunning();
            taskRepository.save(task);

            // Carregar contexto da fase, se disponível
            PhaseContext phaseContext = loadPhaseContext(task);

            // Configurar RAG com filtros da fase
            RetrievalAugmentationAdvisor ragAdvisor = configureRagAdvisor(task, phaseContext);

            // Preparar prompt do sistema
            String systemPrompt = buildSystemPrompt(task, phaseContext);

            // Filtrar tools baseado na política da fase
            ChatClient contextualizedClient = toolPolicy.applyPolicy(chatClient, phaseContext);

            log.debug("Executando com contexto de fase: {}",
                    phaseContext != null ? phaseContext.getPhaseName() : "DEFAULT");

            // Executar com Spring AI
            var response = contextualizedClient.prompt()
                    .system(systemPrompt)
                    .advisors(ragAdvisor)
                    .user(task.getPrompt())
                    .call()
                    .content();

            // Marcar como sucesso
            task.markSucceeded(response);
            log.info("Task {} executada com sucesso", task.getId());

        } catch (Exception e) {
            log.error("Erro ao executar task {}: {}", task.getId(), e.getMessage(), e);
            task.markFailed("Erro na execução: " + e.getMessage());
        }

        return taskRepository.save(task);
    }

    private PhaseContext loadPhaseContext(Task task) {
        if (task.getFlowId() == null) {
            return null;
        }

        Optional<Flow> flowOpt = flowRepository.findById(task.getFlowId());
        if (flowOpt.isEmpty()) {
            log.warn("Flow {} não encontrado para task {}", task.getFlowId(), task.getId());
            return null;
        }

        Flow flow = flowOpt.get();
        Optional<PhaseContext> contextOpt = phaseContextRepository
                .findByFlowIdAndPhaseName(flow.getId(), flow.getCurrentPhase());

        if (contextOpt.isEmpty()) {
            log.warn("Contexto não encontrado para fase {} do flow {}",
                    flow.getCurrentPhase(), flow.getId());
            return null;
        }

        return contextOpt.get();
    }

    private RetrievalAugmentationAdvisor configureRagAdvisor(Task task, PhaseContext phaseContext) {
        if (phaseContext != null && phaseContext.hasRagFilter()) {
            // Usar RAG advisor com filtro personalizado da fase
            String combinedFilter = String.format("tenant == '%s' && (%s)",
                    task.getTenant(), phaseContext.getRagFilter());
            return ragAdvisorConfig.createRagAdvisorWithFilter(combinedFilter);
        } else {
            // Usar RAG advisor padrão com filtro por tenant
            return ragAdvisorConfig.createRagAdvisorForPhase(task.getTenant(), "DEFAULT");
        }
    }

    private String buildSystemPrompt(Task task, PhaseContext phaseContext) {
        String defaultPrompt = "Você é um agente executor de tarefas. " +
                "Planeje brevemente suas ações e use as ferramentas disponíveis quando necessário. " +
                "Mantenha um registro das ações executadas.";

        if (phaseContext == null || !phaseContext.hasSystemPromptTemplate()) {
            return defaultPrompt;
        }

        try {
            // Preparar variáveis para o template
            Map<String, Object> templateVars = Map.of(
                    "tenant", task.getTenant(),
                    "taskId", task.getId().toString(),
                    "flowId", task.getFlowId() != null ? task.getFlowId().toString() : "",
                    "phaseName", phaseContext.getPhaseName(),
                    "timestamp", LocalDateTime.now().toString()
            );

            // Adicionar variáveis do contexto
            if (phaseContext.getVariables() != null) {
                templateVars = new java.util.HashMap<>(templateVars);
                templateVars.putAll(phaseContext.getVariables());
            }

            return templateEngine.render(phaseContext.getSystemPromptTemplate(), templateVars);

        } catch (Exception e) {
            log.warn("Erro ao renderizar template de prompt: {}", e.getMessage());
            return defaultPrompt;
        }
    }
}