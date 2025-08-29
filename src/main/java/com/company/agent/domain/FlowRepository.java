package com.company.agent.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FlowRepository {

    Flow save(Flow flow);

    Optional<Flow> findById(UUID id);

    List<Flow> findByTenant(String tenant);

    Optional<Flow> findByTenantAndName(String tenant, String name);

    void deleteById(UUID id);

    boolean existsById(UUID id);
}
