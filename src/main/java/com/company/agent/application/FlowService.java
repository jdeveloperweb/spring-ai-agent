package com.company.agent.application;

import com.company.agent.domain.Flow;
import com.company.agent.domain.FlowRepository;
import com.company.agent.domain.PhaseContext;
import com.company.agent.domain.PhaseContextRepository;
import com.company.agent.infrastructure.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlowService {

    private final FlowRepository flowRepository;
    private final PhaseContextRepository phaseContextRepository;
    private final TenantContext tenantContext;

    @Transactional
    public Flow createFlow(String name, String description, String initialPhase) {
        String tenant = tenantContext.getCurrentTenant();

        // Verificar se já existe um flow com o mesmo nome
        if (flowRepository.findByTenantAndName(tenant, name).isPresent()) {
            throw new IllegalArgumentException("Já existe um flow com o nome: " + name);
        }

        Flow flow = Flow.builder()
                .id(UUID.randomUUID())
                .name(name)
                .description(description)
                .tenant(tenant)
                .currentPhase(initialPhase)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Flow savedFlow = flowRepository.save(flow);

        log.info("Flow {} criado para tenant {} na fase inicial {}",
                savedFlow.getId(), tenant, initialPhase);

        return savedFlow;
    }

    @Transactional(readOnly = true)
    public Flow getFlow(UUID id) {
        String tenant = tenantContext.getCurrentTenant();

        return flowRepository.findById(id)
                .filter(flow -> tenant.equals(flow.getTenant()))
                .orElseThrow(() -> new IllegalArgumentException("Flow não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<Flow> listFlows() {
        String tenant = tenantContext.getCurrentTenant();
        return flowRepository.findByTenant(tenant);
    }

    @Transactional
    public Flow advanceFlow(UUID flowId, String targetPhase, Map<String, Object> guardVars) {
        String tenant = tenantContext.getCurrentTenant();

        Flow flow = flowRepository.findById(flowId)
                .filter(f -> tenant.equals(f.getTenant()))
                .orElseThrow(() -> new IllegalArgumentException("Flow não encontrado: " + flowId));

        // Validar se pode avançar (guards)
        if (!canAdvanceToPhase(flow, targetPhase, guardVars)) {
            throw new IllegalStateException(
                    String.format("Não é possível avançar de %s para %s",
                            flow.getCurrentPhase(), targetPhase)
            );
        }

        String previousPhase = flow.getCurrentPhase();
        flow.advanceToPhase(targetPhase);

        Flow savedFlow = flowRepository.save(flow);

        log.info("Flow {} avançado de {} para {} no tenant {}",
                flowId, previousPhase, targetPhase, tenant);

        return savedFlow;
    }

    @Transactional
    public PhaseContext upsertPhaseContext(UUID flowId, String phaseName,
                                           Map<String, Object> variables,
                                           Map<String, Object> toolPolicy,
                                           String ragFilter,
                                           String systemPromptTemplate) {
        String tenant = tenantContext.getCurrentTenant();

        // Verificar se o flow existe e pertence ao tenant
        Flow flow = flowRepository.findById(flowId)
                .filter(f -> tenant.equals(f.getTenant()))
                .orElseThrow(() -> new IllegalArgumentException("Flow não encontrado: " + flowId));

        // Buscar contexto existente ou criar novo
        PhaseContext context = phaseContextRepository
                .findByFlowIdAndPhaseName(flowId, phaseName)
                .orElse(PhaseContext.builder()
                        .id(UUID.randomUUID())
                        .flowId(flowId)
                        .phaseName(phaseName)
                        .createdAt(LocalDateTime.now())
                        .build());

        // Atualizar campos
        context.setVariables(variables);
        context.setToolPolicy(toolPolicy);
        context.setRagFilter(ragFilter);
        context.setSystemPromptTemplate(systemPromptTemplate);
        context.setUpdatedAt(LocalDateTime.now());

        PhaseContext savedContext = phaseContextRepository.save(context);

        log.info("Contexto da fase {} atualizado para flow {} no tenant {}",
                phaseName, flowId, tenant);

        return savedContext;
    }

    @Transactional(readOnly = true)
    public PhaseContext getPhaseContext(UUID flowId, String phaseName) {
        String tenant = tenantContext.getCurrentTenant();

        // Verificar se o flow existe e pertence ao tenant
        flowRepository.findById(flowId)
                .filter(f -> tenant.equals(f.getTenant()))
                .orElseThrow(() -> new IllegalArgumentException("Flow não encontrado: " + flowId));

        return phaseContextRepository.findByFlowIdAndPhaseName(flowId, phaseName)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Contexto não encontrado para fase " + phaseName + " do flow " + flowId));
    }

    @Transactional(readOnly = true)
    public List<PhaseContext> getAllPhaseContexts(UUID flowId) {
        String tenant = tenantContext.getCurrentTenant();

        // Verificar se o flow existe e pertence ao tenant
        flowRepository.findById(flowId)
                .filter(f -> tenant.equals(f.getTenant()))
                .orElseThrow(() -> new IllegalArgumentException("Flow não encontrado: " + flowId));

        return phaseContextRepository.findByFlowId(flowId);
    }

    // Guards para validação de transições
    private boolean canAdvanceToPhase(Flow flow, String targetPhase, Map<String, Object> guardVars) {
        // Implementação básica - pode ser expandida com regras complexas
        if (targetPhase == null || targetPhase.equals(flow.getCurrentPhase())) {
            return false;
        }

        // Exemplo de guards básicos
        String currentPhase = flow.getCurrentPhase();

        return switch (currentPhase) {
            case "DISCOVERY" -> "VALIDATION".equals(targetPhase) || "CANCELLED".equals(targetPhase);
            case "VALIDATION" -> "EXECUTION".equals(targetPhase) || "DISCOVERY".equals(targetPhase) || "CANCELLED".equals(targetPhase);
            case "EXECUTION" -> "REVIEW".equals(targetPhase) || "FAILED".equals(targetPhase);
            case "REVIEW" -> "COMPLETED".equals(targetPhase) || "EXECUTION".equals(targetPhase);
            default -> true; // Permite transições livres para fases não mapeadas
        };
    }
}