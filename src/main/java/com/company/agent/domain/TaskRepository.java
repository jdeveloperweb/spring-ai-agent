package com.company.agent.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository {

    Task save(Task task);

    Optional<Task> findById(UUID id);

    List<Task> findByTenant(String tenant);

    List<Task> findByTenantAndStatus(String tenant, Task.Status status);

    List<Task> findByFlowId(UUID flowId);

    void deleteById(UUID id);

    boolean existsById(UUID id);
}
