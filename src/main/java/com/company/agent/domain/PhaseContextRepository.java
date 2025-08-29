package com.company.agent.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PhaseContextRepository {

    PhaseContext save(PhaseContext phaseContext);

    Optional<PhaseContext> findById(UUID id);

    Optional<PhaseContext> findByFlowIdAndPhaseName(UUID flowId, String phaseName);

    List<PhaseContext> findByFlowId(UUID flowId);

    void deleteById(UUID id);

    boolean existsByFlowIdAndPhaseName(UUID flowId, String phaseName);
}
