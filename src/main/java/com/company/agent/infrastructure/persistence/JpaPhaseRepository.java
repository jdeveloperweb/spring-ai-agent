package com.company.agent.infrastructure.persistence;

import com.company.agent.domain.Phase;
import com.company.agent.domain.PhaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaPhaseRepository implements PhaseRepository {

    private final SpringDataPhaseRepository springDataRepository;

    @Override
    public Phase save(Phase phase) {
        PhaseEntity entity = PhaseEntity.fromDomain(phase);
        PhaseEntity saved = springDataRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Phase> findById(UUID id) {
        return springDataRepository.findById(id)
                .map(PhaseEntity::toDomain);
    }

    @Override
    public Optional<Phase> findByTenantAndName(String tenant, String name) {
        return springDataRepository.findByTenantAndName(tenant, name)
                .map(PhaseEntity::toDomain);
    }

    @Override
    public List<Phase> findByTenant(String tenant) {
        return springDataRepository.findByTenantOrderByOrderIndexAsc(tenant)
                .stream()
                .map(PhaseEntity::toDomain)
                .toList();
    }

    @Override
    public List<Phase> findByTenantAndStatus(String tenant, Phase.Status status) {
        return springDataRepository.findByTenantAndStatusOrderByOrderIndexAsc(tenant, status)
                .stream()
                .map(PhaseEntity::toDomain)
                .toList();
    }

    @Override
    public List<Phase> findByTenantOrderByOrderIndex(String tenant) {
        return springDataRepository.findByTenantOrderByOrderIndexAsc(tenant)
                .stream()
                .map(PhaseEntity::toDomain)
                .toList();
    }

    @Override
    public List<Phase> findByCategory(String category) {
        return springDataRepository.findByCategoryOrderByOrderIndexAsc(category)
                .stream()
                .map(PhaseEntity::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        springDataRepository.deleteById(id);
    }

    @Override
    public boolean existsByTenantAndName(String tenant, String name) {
        return springDataRepository.existsByTenantAndName(tenant, name);
    }

    // Spring Data JPA Repository Interface
    interface SpringDataPhaseRepository extends JpaRepository<PhaseEntity, UUID> {

        Optional<PhaseEntity> findByTenantAndName(String tenant, String name);

        List<PhaseEntity> findByTenantOrderByOrderIndexAsc(String tenant);

        List<PhaseEntity> findByTenantAndStatusOrderByOrderIndexAsc(String tenant, Phase.Status status);

        List<PhaseEntity> findByCategoryOrderByOrderIndexAsc(String category);

        boolean existsByTenantAndName(String tenant, String name);
    }
}
