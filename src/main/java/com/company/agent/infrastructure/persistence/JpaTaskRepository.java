package com.company.agent.infrastructure.persistence;

import com.company.agent.domain.Task;
import com.company.agent.domain.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaTaskRepository implements TaskRepository {

    private final SpringDataTaskRepository springDataRepository;

    @Override
    public Task save(Task task) {
        TaskEntity entity = TaskEntity.fromDomain(task);
        TaskEntity saved = springDataRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Task> findById(UUID id) {
        return springDataRepository.findById(id)
                .map(TaskEntity::toDomain);
    }

    @Override
    public List<Task> findByTenant(String tenant) {
        return springDataRepository.findByTenantOrderByCreatedAtDesc(tenant)
                .stream()
                .map(TaskEntity::toDomain)
                .toList();
    }

    @Override
    public List<Task> findByTenantAndStatus(String tenant, Task.Status status) {
        return springDataRepository.findByTenantAndStatusOrderByCreatedAtDesc(tenant, status)
                .stream()
                .map(TaskEntity::toDomain)
                .toList();
    }

    @Override
    public List<Task> findByFlowId(UUID flowId) {
        return springDataRepository.findByFlowIdOrderByCreatedAtDesc(flowId)
                .stream()
                .map(TaskEntity::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        springDataRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return springDataRepository.existsById(id);
    }

    // Spring Data JPA Repository Interface
    interface SpringDataTaskRepository extends JpaRepository<TaskEntity, UUID> {

        List<TaskEntity> findByTenantOrderByCreatedAtDesc(String tenant);

        List<TaskEntity> findByTenantAndStatusOrderByCreatedAtDesc(String tenant, Task.Status status);

        List<TaskEntity> findByFlowIdOrderByCreatedAtDesc(UUID flowId);

        @Query("SELECT t FROM TaskEntity t WHERE t.tenant = :tenant AND t.status = 'RUNNING'")
        List<TaskEntity> findRunningTasksByTenant(@Param("tenant") String tenant);
    }
}
