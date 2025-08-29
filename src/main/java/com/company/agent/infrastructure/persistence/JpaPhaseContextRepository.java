package com.company.agent.infrastructure.persistence;

import com.company.agent.domain.PhaseContext;
import com.company.agent.domain.PhaseContextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaPhaseContextRepository implements PhaseContextRepository {

    private final SpringDataPhaseContextRepository springDataRepository;

    @Override
    public PhaseContext save(PhaseContext phaseContext) {
        PhaseContextEntity entity = PhaseContextEntity.fromDomain(phaseContext);
        PhaseContextEntity saved = springDataRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<PhaseContext> findById(UUID id) {
        return springDataRepository.findById(id)
                .map(PhaseContextEntity::toDomain);
    }

    @Override
    public Optional<PhaseContext> findByFlowIdAndPhaseName(UUID flowId, String phaseName) {
        return springDataRepository.findByFlowIdAndPhaseName(flowId, phaseName)
                .map(PhaseContextEntity::toDomain);
    }

    @Override
    public List<PhaseContext> findByFlowId(UUID flowId) {
        return springDataRepository.findByFlowIdOrderByPhaseNameAsc(flowId)
                .stream()
                .map(PhaseContextEntity::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        springDataRepository.deleteById(id);
    }

    @Override
    public boolean existsByFlowIdAndPhaseName(UUID flowId, String phaseName) {
        return springDataRepository.existsByFlowIdAndPhaseName(flowId, phaseName);
    }

    // Spring Data JPA Repository Interface
    interface SpringDataPhaseContextRepository extends JpaRepository<PhaseContextEntity, UUID> {

        Optional<PhaseContextEntity> findByFlowIdAndPhaseName(UUID flowId, String phaseName);

        List<PhaseContextEntity> findByFlowIdOrderByPhaseNameAsc(UUID flowId);

        boolean existsByFlowIdAndPhaseName(UUID flowId, String phaseName);
    }
}
