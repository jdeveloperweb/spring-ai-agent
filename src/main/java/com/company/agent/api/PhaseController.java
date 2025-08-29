package com.company.agent.api;

import com.company.agent.api.dto.PhaseContextDto;
import com.company.agent.api.dto.PhaseDto;
import com.company.agent.api.dto.CreatePhaseRequest;
import com.company.agent.api.dto.UpdatePhaseRequest;
import com.company.agent.application.PhaseManager;
import com.company.agent.domain.Phase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/phases")
@RequiredArgsConstructor
@Slf4j
public class PhaseController {

    private final PhaseManager phaseManager;

    @PostMapping
    public ResponseEntity<PhaseDto> createPhase(@Valid @RequestBody CreatePhaseRequest request) {
        log.info("Criando fase: name='{}', category='{}'",
                request.getName(), request.getCategory());

        Phase phase = phaseManager.createPhase(
                request.getName(),
                request.getDescription(),
                request.getCategory(),
                request.getOrderIndex()
        );

        return ResponseEntity.ok(PhaseDto.fromDomain(phase));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhaseContextDto> getPhase(@PathVariable UUID id) {
        Phase phase = phaseManager.getPhase(id);
        return ResponseEntity.ok(PhaseDto.fromDomain(phase));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<PhaseDto> getPhaseByName(@PathVariable String name) {
        Phase phase = phaseManager.getPhaseByName(name);
        return ResponseEntity.ok(PhaseDto.fromDomain(phase));
    }

    @GetMapping
    public ResponseEntity<List<PhaseDto>> listPhases(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category) {

        List<Phase> phases;

        if (category != null) {
            phases = phaseManager.listPhasesByCategory(category);
        } else if ("ACTIVE".equals(status)) {
            phases = phaseManager.listActivePhases();
        } else {
            phases = phaseManager.listPhases();
        }

        List<PhaseDto> response = phases.stream()
                .map(PhaseDto::fromDomain)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PhaseDto> updatePhase(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePhaseRequest request) {

        log.info("Atualizando fase {}", id);

        Phase phase = phaseManager.updatePhase(
                id,
                request.getDescription(),
                request.getCategory(),
                request.getOrderIndex()
        );

        return ResponseEntity.ok(PhaseDto.fromDomain(phase));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activatePhase(@PathVariable UUID id) {
        phaseManager.activatePhase(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivatePhase(@PathVariable UUID id) {
        phaseManager.deactivatePhase(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhase(@PathVariable UUID id) {
        phaseManager.deletePhase(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/transitions")
    public ResponseEntity<Map<String, Object>> checkTransition(
            @RequestParam String from,
            @RequestParam String to) {

        boolean canTransition = phaseManager.canTransition(from, to);

        return ResponseEntity.ok(Map.of(
                "from", from,
                "to", to,
                "canTransition", canTransition
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.status(409) // Conflict
                .body(Map.of("error", e.getMessage()));
    }
}
