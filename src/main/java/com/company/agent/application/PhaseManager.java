package com.company.agent.application;

import com.company.agent.domain.Phase;
import com.company.agent.domain.PhaseRepository;
import com.company.agent.infrastructure.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhaseManager {

    private final PhaseRepository phaseRepository;
    private final TenantContext tenantContext;

    @Transactional
    public Phase createPhase(String name, String description, String category, Integer orderIndex) {
        String tenant = tenantContext.getCurrentTenant();

        // Verificar se já existe uma fase com o mesmo nome
        if (phaseRepository.existsByTenantAndName(tenant, name)) {
            throw new IllegalArgumentException("Já existe uma fase com o nome: " + name);
        }

        Phase phase = Phase.builder()
                .id(UUID.randomUUID())
                .name(name)
                .description(description)
                .tenant(tenant)
                .status(Phase.Status.ACTIVE)
                .category(category)
                .orderIndex(orderIndex != null ? orderIndex : getNextOrderIndex(tenant))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Phase savedPhase = phaseRepository.save(phase);

        log.info("Fase {} criada para tenant {} na categoria {}",
                savedPhase.getName(), tenant, category);

        return savedPhase;
    }

    @Transactional(readOnly = true)
    public Phase getPhase(UUID id) {
        String tenant = tenantContext.getCurrentTenant();

        return phaseRepository.findById(id)
                .filter(phase -> tenant.equals(phase.getTenant()))
                .orElseThrow(() -> new IllegalArgumentException("Fase não encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public Phase getPhaseByName(String name) {
        String tenant = tenantContext.getCurrentTenant();

        return phaseRepository.findByTenantAndName(tenant, name)
                .orElseThrow(() -> new IllegalArgumentException("Fase não encontrada: " + name));
    }

    @Transactional(readOnly = true)
    public List<Phase> listPhases() {
        String tenant = tenantContext.getCurrentTenant();
        return phaseRepository.findByTenant(tenant);
    }

    @Transactional(readOnly = true)
    public List<Phase> listActivePhases() {
        String tenant = tenantContext.getCurrentTenant();
        return phaseRepository.findByTenantAndStatus(tenant, Phase.Status.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<Phase> listPhasesByCategory(String category) {
        return phaseRepository.findByCategory(category);
    }

    @Transactional
    public Phase updatePhase(UUID id, String description, String category, Integer orderIndex) {
        String tenant = tenantContext.getCurrentTenant();

        Phase phase = phaseRepository.findById(id)
                .filter(p -> tenant.equals(p.getTenant()))
                .orElseThrow(() -> new IllegalArgumentException("Fase não encontrada: " + id));

        if (description != null) {
            phase.setDescription(description);
        }

        if (category != null) {
            phase.setCategory(category);
        }

        if (orderIndex != null) {
            phase.setOrderIndex(orderIndex);
        }

        phase.setUpdatedAt(LocalDateTime.now());

        Phase savedPhase = phaseRepository.save(phase);

        log.info("Fase {} atualizada para tenant {}", phase.getName(), tenant);

        return savedPhase;
    }

    @Transactional
    public void activatePhase(UUID id) {
        String tenant = tenantContext.getCurrentTenant();

        Phase phase = phaseRepository.findById(id)
                .filter(p -> tenant.equals(p.getTenant()))
                .orElseThrow(() -> new IllegalArgumentException("Fase não encontrada: " + id));

        phase.activate();
        phaseRepository.save(phase);

        log.info("Fase {} ativada para tenant {}", phase.getName(), tenant);
    }

    @Transactional
    public void deactivatePhase(UUID id) {
        String tenant = tenantContext.getCurrentTenant();

        Phase phase = phaseRepository.findById(id)
                .filter(p -> tenant.equals(p.getTenant()))
                .orElseThrow(() -> new IllegalArgumentException("Fase não encontrada: " + id));

        phase.deactivate();
        phaseRepository.save(phase);

        log.info("Fase {} desativada para tenant {}", phase.getName(), tenant);
    }

    @Transactional
    public void deletePhase(UUID id) {
        String tenant = tenantContext.getCurrentTenant();

        Phase phase = phaseRepository.findById(id)
                .filter(p -> tenant.equals(p.getTenant()))
                .orElseThrow(() -> new IllegalArgumentException("Fase não encontrada: " + id));

        // Verificar se a fase pode ser deletada (não está sendo usada)
        // Esta verificação pode ser expandida para checar uso em flows ativos

        phaseRepository.deleteById(id);

        log.info("Fase {} deletada para tenant {}", phase.getName(), tenant);
    }

    private Integer getNextOrderIndex(String tenant) {
        List<Phase> phases = phaseRepository.findByTenantOrderByOrderIndex(tenant);

        if (phases.isEmpty()) {
            return 1;
        }

        return phases.get(phases.size() - 1).getOrderIndex() + 1;
    }

    // Métodos utilitários para validação de transições
    public boolean canTransition(String fromPhase, String toPhase) {
        String tenant = tenantContext.getCurrentTenant();

        try {
            Phase from = getPhaseByName(fromPhase);
            Phase to = getPhaseByName(toPhase);

            // Verificar se ambas as fases estão ativas
            if (!from.isActive() || !to.isActive()) {
                return false;
            }

            // Lógica básica de transição baseada em ordem
            return to.getOrderIndex() != null && from.getOrderIndex() != null &&
                    (to.getOrderIndex() > from.getOrderIndex() || // Avançar
                            to.getOrderIndex() < from.getOrderIndex());   // Retroceder

        } catch (IllegalArgumentException e) {
            log.warn("Erro ao validar transição de {} para {}: {}", fromPhase, toPhase, e.getMessage());
            return false;
        }
    }
}
