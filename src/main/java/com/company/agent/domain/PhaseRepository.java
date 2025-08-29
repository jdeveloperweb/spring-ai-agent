package com.company.agent.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PhaseRepository {

    Phase save(Phase phase);

    Optional<Phase> findById(UUID id);

    Optional<Phase> findByTenantAndName(String tenant, String name);

    List<Phase> findByTenant(String tenant);

    List<Phase> findByTenantAndStatus(String tenant, Phase.Status status);

    List<Phase> findByTenantOrderByOrderIndex(String tenant);

    List<Phase> findByCategory(String category);

    void deleteById(UUID id);

    boolean existsByTenantAndName(String tenant, String name);
}
