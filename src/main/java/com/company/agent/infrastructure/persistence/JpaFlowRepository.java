package com.company.agent.infrastructure.persistence;

import com.company.agent.domain.Flow;
import com.company.agent.domain.FlowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaFlowRepository implements FlowRepository {

    private final SpringDataFlowRepository springDataRepository;

    @Override
    public Flow save(Flow flow) {
        FlowEntity entity = FlowEntity.fromDomain(flow);
        FlowEntity saved = springDataRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Flow> findById(UUID id) {
        return springDataRepository.findById(id)
                .map(FlowEntity::toDomain);
    }

    @Override
    public List<Flow> findByTenant(String tenant) {
        return springDataRepository.findByTenantOrderByCreatedAtDesc(tenant)
                .stream()
                .map(FlowEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Flow> findByTenantAndName(String tenant, String name) {
        return springDataRepository.findByTenantAndName(tenant, name)
                .map(FlowEntity::toDomain);
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
    interface SpringDataFlowRepository extends JpaRepository<FlowEntity, UUID> {

        List<FlowEntity> findByTenantOrderByCreatedAtDesc(String tenant);

        Optional<FlowEntity> findByTenantAndName(String tenant, String name);
    }
}
