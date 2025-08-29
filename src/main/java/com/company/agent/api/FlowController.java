package com.company.agent.api;

import com.company.agent.api.dto.*;
import com.company.agent.application.FlowService;
import com.company.agent.domain.Flow;
import com.company.agent.domain.PhaseContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/flows")
@RequiredArgsConstructor
@Slf4j
public class FlowController {

    private final FlowService flowService;

    @PostMapping
    public ResponseEntity<FlowDto> createFlow(@Valid @RequestBody CreateFlowRequest request) {
        log.info("Criando flow: name='{}', initialPhase='{}'",
                request.getName(), request.getInitialPhase());

        Flow flow = flowService.createFlow(
                request.getName(),
                request.getDescription(),
                request.getInitialPhase()
        );

        return ResponseEntity.ok(FlowDto.fromDomain(flow));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlowDto> getFlow(@PathVariable UUID id) {
        Flow flow = flowService.getFlow(id);
        return ResponseEntity.ok(FlowDto.fromDomain(flow));
    }

    @GetMapping
    public ResponseEntity<List<FlowDto>> listFlows() {
        List<Flow> flows = flowService.listFlows();

        List<FlowDto> response = flows.stream()
                .map(FlowDto::fromDomain)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}:advance")
    public ResponseEntity<FlowDto> advanceFlow(
            @PathVariable UUID id,
            @Valid @RequestBody AdvanceFlowRequest request) {

        log.info("Avançando flow {} para fase '{}'", id, request.getTo());

        Flow flow = flowService.advanceFlow(id, request.getTo(), request.getGuardVars());

        return ResponseEntity.ok(FlowDto.fromDomain(flow));
    }

    @PutMapping("/{id}/phases/{phase}")
    public ResponseEntity<PhaseContextDto> upsertPhaseContext(
            @PathVariable UUID id,
            @PathVariable String phase,
            @Valid @RequestBody PhaseUpsertRequest request) {

        log.info("Atualizando contexto da fase '{}' do flow {}", phase, id);

        PhaseContext context = flowService.upsertPhaseContext(
                id,
                phase,
                request.getVariables(),
                request.getToolPolicy(),
                request.getRagFilter(),
                request.getSystemPromptTemplate()
        );

        return ResponseEntity.ok(PhaseContextDto.fromDomain(context));
    }

    @GetMapping("/{id}/phases/{phase}")
    public ResponseEntity<PhaseContextDto> getPhaseContext(
            @PathVariable UUID id,
            @PathVariable String phase) {

        PhaseContext context = flowService.getPhaseContext(id, phase);
        return ResponseEntity.ok(PhaseContextDto.fromDomain(context));
    }

    @GetMapping("/{id}/phases")
    public ResponseEntity<List<PhaseContextDto>> getAllPhaseContexts(@PathVariable UUID id) {
        List<PhaseContext> contexts = flowService.getAllPhaseContexts(id);

        List<PhaseContextDto> response = contexts.stream()
                .map(PhaseContextDto::fromDomain)
                .toList();

        return ResponseEntity.ok(response);
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
